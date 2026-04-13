plugins {
    java
    application
}

group = "fish.cichlidmc"
version = "1.0.0"

val agent: Configuration by configurations.creating { isTransitive = false }
val plugin: Configuration by configurations.creating { isTransitive = false }
val mod: Configuration by configurations.creating { isTransitive = false }

dependencies {
    agent(implementation(project(":", configuration = "shadow"))!!)
    plugin(implementation(project(":test-plugin"))!!)
    mod(implementation(project(":test-mod"))!!)
}

application {
    mainClass = "fish.cichlidmc.test_app.Main"
}

tasks.run {
    //jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    jvmArgs("-Xverify:all")

    agent.files.forEach {
        val arg = "-javaagent:$it"
        jvmArgs(arg)
    }

    workingDir = file("run")

    doFirst {
        workingDir.mkdirs()
    }
}
