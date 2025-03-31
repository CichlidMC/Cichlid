rootProject.name = "cichlid"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

file("test").listFiles()!!.forEach {
    include("test-${it.name}")
    project(":test-${it.name}").projectDir = it
}
