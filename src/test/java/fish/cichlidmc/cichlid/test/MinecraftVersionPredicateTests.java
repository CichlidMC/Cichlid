package fish.cichlidmc.cichlid.test;

import fish.cichlidmc.cichlid.api.version.MinecraftVersion;
import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import org.junit.jupiter.api.Test;

import static fish.cichlidmc.cichlid.test.MoreAssertions.assertThrowsWithMessage;

public final class MinecraftVersionPredicateTests {
	@Test
	public void lteStandard() {
		PredicateTester<String> tester = tester("<=26.2");
		tester.assertMatches("26.1", "26.1.1", "26.2-snapshot-1", "26.2");
		tester.assertNotMatching("26.2.1-snapshot-1", "26.2.1", "20w14infinite");
	}

	@Test
	public void gteStandard() {
		PredicateTester<String> tester = tester(">=26.1");
		tester.assertMatches("26.1", "26.1.1", "26.1.1-snapshot-1");
		tester.assertNotMatching("26.1-rc-3", "25w14craftmine");
	}

	@Test
	public void equalStandard() {
		PredicateTester<String> tester = tester("==26.1.1");
		tester.assertMatches("26.1.1");
		tester.assertNotMatching("26.1", "26.1.1-snapshot-1", "26.2", "20w14infinite");
	}

	@Test
	public void notEqualStandard() {
		PredicateTester<String> tester = tester("!=26.2");
		tester.assertNotMatching("26.2");
		tester.assertMatches("26.1.1", "26.2.1", "26.2-snapshot-1", "25w14craftmine");
	}

	@Test
	public void ltStandard() {
		PredicateTester<String> tester = tester("<26.2");
		tester.assertMatches("26.1", "26.1.1", "26.2-snapshot-1");
		tester.assertNotMatching("26.2", "26.2.1-snapshot-1", "26.2.1", "20w14infinite");
	}

	@Test
	public void gtStandard() {
		PredicateTester<String> tester = tester(">26.1");
		tester.assertMatches("26.1.1", "26.1.1-snapshot-1");
		tester.assertNotMatching("26.1", "26.1-rc-3", "25w14craftmine");
	}

	@Test
	public void equalSpecial() {
		PredicateTester<String> tester = tester("==25w14craftmine");
		tester.assertMatches("25w14craftmine");
		tester.assertNotMatching("20w14infinite", "26.1");
	}

	@Test
	public void notEqualSpecial() {
		PredicateTester<String> tester = tester("!=25w14craftmine");
		tester.assertNotMatching("25w14craftmine");
		tester.assertMatches("20w14infinite", "26.1");
	}

	@Test
	public void compareSpecialByEqualityOnly() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Version '25w14craftmine' can only be compared by equality (== or !=): >=25w14craftmine",
				() -> MinecraftVersion.parsePredicate(">=25w14craftmine")
		);
	}

	private static PredicateTester<String> tester(String predicate) {
		return new PredicateTester<>(MinecraftVersion.parsePredicate(predicate)).map(MinecraftVersion::parseAny);
	}
}
