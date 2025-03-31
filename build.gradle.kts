plugins {
    id("com.gradleup.shadow") version "8.3.6"
    id("java-library")
    id("maven-publish")
}

base.archivesName = "cichlid"
group = "io.github.cichlidmc"
version = "0.3.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://mvn.devos.one/snapshots/")
    }
}

// configuration for shadowed dependencies
val shade: Configuration by configurations.creating

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

// dummy sourceSets for features. Contents are manged by jar task configuration below.
val modApi: SourceSet by sourceSets.creating
val pluginApi: SourceSet by sourceSets.creating

java {
    withSourcesJar()

    registerFeature("modApi") {
        withSourcesJar()
        usingSourceSet(modApi)
    }

    registerFeature("pluginApi") {
        withSourcesJar()
        usingSourceSet(pluginApi)
    }
}

// jar: dependencies not bundled, for impl access and dev runtime
// shadowJar: prod, dependencies bundled, for distribution
// modApiJar: mod-api, for mods at compile time
// pluginApiJar: plugin-api, for plugins at compile time

tasks.named<Jar>("jar") {
    manifest.attributes["Premain-Class"] = "io.github.cichlidmc.cichlid.impl.CichlidAgent"
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier = "prod"
    configurations = listOf(shade)
    manifest.attributes["Premain-Class"] = "io.github.cichlidmc.cichlid.impl.CichlidAgent"

    // exclude signatures and manifest of dependencies
    exclude("META-INF/**")
}

tasks.named<Jar>("modApiJar") {
    dependsOn("jar")
    archiveClassifier = "mod-api"
    from(zipTree(files(tasks.named("jar")).singleFile))
    exclude("io/github/cichlidmc/cichlid/impl/**")
    exclude("io/github/cichlidmc/cichlid/api/plugin/**")
    includeEmptyDirs = false
}

tasks.named<Jar>("modApiSourcesJar") {
    dependsOn("sourcesJar")
    archiveClassifier = "mod-api-sources"
    from(zipTree(files(tasks.named("sourcesJar")).singleFile))
    exclude("io/github/cichlidmc/cichlid/impl/**")
    exclude("io/github/cichlidmc/cichlid/api/plugin/**")
    includeEmptyDirs = false
}

tasks.named<Jar>("pluginApiJar") {
    dependsOn("jar")
    archiveClassifier = "plugin-api"
    from(zipTree(files(tasks.named("jar")).singleFile))
    exclude("io/github/cichlidmc/cichlid/impl/**")
    exclude("io/github/cichlidmc/cichlid/api/mod/**")
    includeEmptyDirs = false
}

tasks.named<Jar>("pluginApiSourcesJar") {
    dependsOn("sourcesJar")
    archiveClassifier = "plugin-api-sources"
    from(zipTree(files(tasks.named("sourcesJar")).singleFile))
    exclude("io/github/cichlidmc/cichlid/impl/**")
    exclude("io/github/cichlidmc/cichlid/api/mod/**")
    includeEmptyDirs = false
}

tasks.named("assemble").configure {
    dependsOn("shadowJar", "modApiJar", "pluginApiJar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/snapshots") {
            name = "devOS"
            credentials(PasswordCredentials::class)
        }
    }
}
