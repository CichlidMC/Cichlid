plugins {
    id("java")
    id("application")
}

base.archivesName = "CichlidTestApp"
group = "io.github.cichlidmc"
version = properties["version"]!!

repositories {
    mavenCentral()
}

val agent: Configuration by configurations.creating { isTransitive = false }
val plugin: Configuration by configurations.creating { isTransitive = false }
val mod: Configuration by configurations.creating { isTransitive = false }

dependencies {
    agent(implementation(project(":", configuration = "shadow"))!!)
    plugin(implementation(project(":test-plugin"))!!)
    mod(implementation(project(":test-mod"))!!)
}

application {
    mainClass = "io.github.cichlidmc.test_app.Main"
}

tasks.named("run", JavaExec::class) {
    jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
    jvmArgs("-Xverify:all")

    agent.files.forEach {
        val arg = "-javaagent:$it=dist=client,version=1.21.4"
        jvmArgs(arg)
    }

    workingDir = file("run")

    doFirst {
        workingDir.mkdirs()
    }
}
