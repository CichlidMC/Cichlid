plugins {
    java
}

group = "fish.cichlidmc"
version = "1.0.0"

dependencies {
    implementation(project(":"))
}

tasks.jar {
    archiveExtension = "clp"
}
