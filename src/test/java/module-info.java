// the test sourceSet must be a module for Gradle to treat the main sourceSet as a module.
// we want that so we can verify that the metadata is set correctly.
open module fish.cichlidmc.cichlid.test {
	requires fish.cichlidmc.cichlid;
	requires org.junit.jupiter.api;
}
