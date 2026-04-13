plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
}

group = "fish.cichlidmc"
version = "0.4.0"

// configurations for shadowed dependencies
val shade: Configuration by configurations.creating

dependencies {
    compileOnlyApi(libs.bundles.annotations)
    api(libs.sushi)
    shade(libs.bundles.shadowed)

    compileOnly(libs.log4j.api)

    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
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
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.jar {
    manifest.attributes["Premain-Class"] = "fish.cichlidmc.cichlid.impl.CichlidAgent"
    manifest.attributes["Can-Retransform-Classes"] = "true"
}

tasks.shadowJar {
    archiveClassifier = "prod"
    configurations = listOf(shade)
    manifest.attributes["Premain-Class"] = "fish.cichlidmc.cichlid.impl.CichlidAgent"
    manifest.attributes["Can-Retransform-Classes"] = "true"

    // exclude signatures and manifest of dependencies
    exclude("META-INF/**")

    // I kinda need that, shadow...
    excludes.remove("module-info.class")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.register("buildProd") {
    dependsOn("shadowJar")
    val source = files(tasks.named("shadowJar")).singleFile
    val dest = file("/home/tropheusj/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/cichlid testing/minecraft/cichlid/.meta/cichlid.jar")

    doFirst {
        source.copyTo(dest, overwrite = true)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        listOf("Releases", "Snapshots").forEach {
            maven("https://mvn.devos.one/${it.lowercase()}") {
                name = "devOs$it"
                credentials(PasswordCredentials::class)
            }
        }
    }
}
