plugins {
    id("com.gradleup.shadow") version "8.3.0"
    id("java-library")
    id("maven-publish")
}

base.archivesName = "Cichlid"
group = "io.github.cichlidmc"
version = properties["version"]!!

allprojects {
    repositories {
        mavenCentral()
        maven("https://mvn.devos.one/snapshots/")
    }
}

// configuration for shadowed dependencies
val shade: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:24.1.0")
    shade(api("io.github.cichlidmc:TinyJson:1.0.1")!!)
    shade(api("io.github.cichlidmc:Sushi:0.1.0")!!)
    shade(api("org.ow2.asm:asm-tree:9.7")!!)
    compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named("processResources", ProcessResources::class) {
    val properties = mapOf(
        "version" to version.toString()
    )

    inputs.properties(properties)

    filesMatching("cichlid_version.txt") {
        expand(properties)
    }
}

java {
    withSourcesJar()
}

// jar: slim (no dependencies)
// shadowJar: fat (all dependencies)
// apiJar: api (slim without impls and plugin-api)
// pluginApiJar: plugin-api (slim without impls and api)

tasks.named("jar", Jar::class).configure {
    archiveClassifier = "slim"
}

tasks.named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    configurations = listOf(shade)
    archiveClassifier = ""
    manifest.attributes["Premain-Class"] = "io.github.cichlidmc.cichlid.impl.CichlidAgent"

    // exclude signatures and manifest of dependencies
    exclude("META-INF/**")
}

tasks.register<Jar>("apiJar") {
    dependsOn(tasks.named("jar"))
    archiveClassifier = "api"
    from(zipTree(files(tasks.named("jar")).singleFile))
    include("**/api/**")
    exclude("**/api/plugin/**")
    includeEmptyDirs = false
}

tasks.register<Jar>("pluginApiJar") {
    dependsOn(tasks.named("jar"))
    archiveClassifier = "plugin-api"
    from(zipTree(files(tasks.named("jar")).singleFile))
    include("**/api/**")
    exclude("**/api/mod/**")
    includeEmptyDirs = false
}

tasks.named("assemble").configure {
    dependsOn("shadowJar", "apiJar", "pluginApiJar")
}

// output configuration for fat jar
val fat: Configuration by configurations.creating {
    isCanBeConsumed = true
}

artifacts {
    add("fat", tasks.named("shadowJar"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // default is project name, use archive name instead
            artifactId = base.archivesName.get()
            // includes main and sources jars
            from(components["java"])

            artifact(tasks.named("apiJar")) {
                classifier = "api"
            }
            artifact(tasks.named("pluginApiJar")) {
                classifier = "plugin-api"
            }
        }
    }

    repositories {
        maven("https://mvn.devos.one/snapshots") {
            name = "devOS"
            credentials(PasswordCredentials::class)
        }
    }
}
