plugins {
    id("java")
}

base.archivesName = "CichlidTestPlugin"
group = "fish.cichlidmc"
version = properties["version"]!!

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
}

tasks.named("jar", Jar::class).configure {
    archiveExtension = "clp"
}
