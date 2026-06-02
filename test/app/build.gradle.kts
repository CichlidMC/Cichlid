plugins {
    java
    application
    alias(libs.plugins.javaagent.application)
}

group = "fish.cichlidmc"
version = "1.0.0"

val plugin: Configuration by configurations.creating { isTransitive = false }
val mod: Configuration by configurations.creating { isTransitive = false }

dependencies {
    javaagent(implementation(project(":", configuration = "shadow"))!!)
    plugin(implementation(project(":test-plugin"))!!)
    mod(implementation(project(":test-mod"))!!)
}

application {
    mainClass = "fish.cichlidmc.test_app.Main"
}

tasks.run {
    jvmArgs("-Xverify:all")
    jvmArgs("-Dfish.cichlidmc.cichlid.distribution.override=client")
    jvmArgs("-Dfish.cichlidmc.cichlid.transform.export=true")

    workingDir = file("run")
}
