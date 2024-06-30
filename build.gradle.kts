plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("java-library")
}

group = "io.github.cichlidmc"
version = properties["version"]!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation("com.google.code.gson:gson:2.11.0")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

tasks.named("jar", Jar::class).configure {
    archiveClassifier = "slim"
}

tasks.named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveClassifier = ""
    manifest.attributes["Premain-Class"] = "io.github.cichlidmc.cichlid_loader.impl.CichlidAgent"

    relocate("com.google", "io.github.cichlidmc.cichlid_loader.impl.shadow.google")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
