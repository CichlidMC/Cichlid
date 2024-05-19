plugins {
    id("java-library")
}

group = "io.github.tropheusj"
version = properties["version"]!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
}

tasks.named("jar", Jar::class).configure {
    manifest.attributes["Premain-Class"] = "io.github.tropheusj.cichlid.impl.CichlidAgent"
}
