package fish.cichlidmc.cichlid.test;

import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static fish.cichlidmc.cichlid.test.MoreAssertions.assertThrowsWithMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModVersionPredicateTests {
	@Test
	public void testLe() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("<=1.20");
		assertTrue(test(predicate, "1.19"));
		assertTrue(test(predicate, "1.20"));
		assertTrue(test(predicate, "1.20-beta.1"));
		assertTrue(test(predicate, "1.20-pre1"));
		assertTrue(test(predicate, "1.20-a"));
		assertFalse(test(predicate, "1.20.1"));
	}

	@Test
	public void testGe() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(">=1.12.2");
		assertFalse(test(predicate, "1.12.2-beta.5"));
		assertFalse(test(predicate, "1.12.1.1.1"));
		assertFalse(test(predicate, "1.12.1+build.100"));
		assertTrue(test(predicate, "1.12.2"));
		assertTrue(test(predicate, "1.12.2.1"));
		assertTrue(test(predicate, "1.13.1"));
		assertTrue(test(predicate, "1.12.2e"));
	}

	@Test
	public void testEqual() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(" ==  1.12.2");
		assertFalse(test(predicate, "1.12.2-beta.5"));
		assertFalse(test(predicate, "1.12.1.1.1"));
		assertFalse(test(predicate, "1.12.1+build.100"));
		assertTrue(test(predicate, "1.12.2"));
		assertFalse(test(predicate, "1.12.2.1"));
		assertFalse(test(predicate, "1.13.1"));
		assertFalse(test(predicate, "1.12.2e"));
	}

	@Test
	public void testNotEqual() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("!= 1.12.2  ");
		assertTrue(test(predicate, "1.12.2-beta.5"));
		assertTrue(test(predicate, "1.12.1.1.1"));
		assertTrue(test(predicate, "1.12.1+build.100"));
		assertFalse(test(predicate, "1.12.2"));
		assertTrue(test(predicate, "1.12.2.1"));
		assertTrue(test(predicate, "1.13.1"));
		assertTrue(test(predicate, "1.12.2e"));
	}

	@Test
	public void testLt() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("<1.12.2");
		assertTrue(test(predicate, "1.12.2-beta.5"));
		assertTrue(test(predicate, "1.12.1.1.1"));
		assertTrue(test(predicate, "1.12.1+build.100"));
		assertFalse(test(predicate, "1.12.2"));
		assertFalse(test(predicate, "1.12.2.1"));
		assertFalse(test(predicate, "1.13.1"));
		assertFalse(test(predicate, "1.12.2e"));
	}

	@Test
	public void testGt() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("> 1.12.2");
		assertFalse(test(predicate, "1.12.2-beta.5"));
		assertFalse(test(predicate, "1.12.1.1.1"));
		assertFalse(test(predicate, "1.12.1+build.100"));
		assertFalse(test(predicate, "1.12.2"));
		assertTrue(test(predicate, "1.12.2.1"));
		assertTrue(test(predicate, "1.13.1"));
		assertTrue(test(predicate, "1.12.2e"));
	}

	@Test
	public void testAny() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("any ");
		assertTrue(test(predicate, "1.12"));
		assertTrue(test(predicate, "1.9.4"));
		assertTrue(test(predicate, "1.3.1.2-build.5"));
		assertTrue(test(predicate, "22w35a"));
		assertTrue(test(predicate, "aaaaaa"));
		assertEquals("any", predicate.toString());
	}

	@Test
	public void testCreateLike() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(">=0.5.1.i");
		assertTrue(test(predicate, "0.5.1.i"));
		assertTrue(test(predicate, "0.5.1.j"));
		assertTrue(test(predicate, "0.5.2"));
		assertTrue(test(predicate, "0.5.2.a"));
		assertFalse(test(predicate, "0.5.0"));
		assertFalse(test(predicate, "0.5.1.h"));
	}

	@Test
	public void testCreateFabricLike() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(">=0.5.1-i");
		assertTrue(test(predicate, "0.5.1-i-build.1000+mc1.20"));
		assertTrue(test(predicate, "0.5.1-j"));
		assertTrue(test(predicate, "0.5.2-a"));
		assertTrue(test(predicate, "0.5.2-i"));
		assertFalse(test(predicate, "0.5.0-i"));
		assertFalse(test(predicate, "0.5.1-h"));
	}

	@Test
	public void testSnapshot() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("<=24w25a");
		assertTrue(test(predicate, "23w25a"));
		assertTrue(test(predicate, "24w23a"));
		assertFalse(test(predicate, "25w12a"));
		assertFalse(test(predicate, "24w30a"));
		assertFalse(test(predicate, "24w25b"));
	}

	@Test
	public void testNested() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("(((((>=1.20)))))");
		assertTrue(test(predicate, "1.20"));
		assertTrue(test(predicate, "1.21"));
		assertFalse(test(predicate, "1.19"));
	}

	@Test
	public void testAnd() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(">=1.16 && <1.17");
		assertTrue(test(predicate, "1.16"));
		assertTrue(test(predicate, "1.16.5"));
		assertTrue(test(predicate, "1.16.3"));
		assertFalse(test(predicate, "1.17"));
		assertTrue(test(predicate, "1.17-alpha.1"));
	}

	@Test
	public void testOr() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(">=1.16 || ==1.14.2");
		assertTrue(test(predicate, "1.16"));
		assertTrue(test(predicate, "1.16.5"));
		assertFalse(test(predicate, "1.14"));
		assertFalse(test(predicate, "1.14.1"));
		assertTrue(test(predicate, "1.14.2"));
	}

	@Test
	public void testComplex() {
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("(>=1.14 && (<= 1.14.4 || ==1.15.2)) || >22w13a &&<22w13c");
		assertTrue(test(predicate, "1.14"));
		assertTrue(test(predicate, "1.14.2"));
		assertTrue(test(predicate, "1.14.4"));
		assertFalse(test(predicate, "1.15.1"));
		assertTrue(test(predicate, "1.15.2"));
		assertFalse(test(predicate, "22w13a"));
		assertTrue(test(predicate, "22w13b"));
		assertFalse(test(predicate, "22w13c"));
	}

	@Test
	public void testPrecedence() {
		// if the grouping is (!=22w35a || >23w15b) && <23w17c, then it simplifies to !=22w35a && <23w17c (wrong)
		// if the grouping is !=22w35a || (>23w15b && <23w17c), then it simplifies to !=22w35a (right)
		Predicate<ModVersion> predicate = ModVersion.parsePredicate("!=22w35a || >23w15b && <23w17c");
		assertTrue(test(predicate, "12w01a"));
		assertTrue(test(predicate, "99w52z"));
		assertFalse(test(predicate, "22w35a"));
	}

	@Test
	public void testToString() {
		String input = "(>=1.14 && (<= 1.14.4 || ==1.15.2)) || >22w13a &&<22w13c";
		// more parentheses than necessary, but it's Fine
		String expected = "((>=1.14 && (<=1.14.4 || ==1.15.2)) || (>22w13a && <22w13c))";
		Predicate<ModVersion> predicate = ModVersion.parsePredicate(input);
		assertEquals(expected, predicate.toString());
	}

	@Test
	public void testEmpty() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Version predicate cannot be empty",
				() -> ModVersion.parsePredicate("   ")
		);
	}

	@Test
	public void testExtraOpeningParenthesis() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"More opening parentheses than closing ones: (>1.4 && !=1.4.2) || (==22w14a",
				() -> ModVersion.parsePredicate("(>1.4 && !=1.4.2) || (==22w14a")
		);
	}

	@Test
	public void testSwappedParentheses() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Closing/opening parenthesis mismatch: )>1.4 && !=1.4.2(",
				() -> ModVersion.parsePredicate(")>1.4 && !=1.4.2(")
		);
	}

	@Test
	public void testParenthesesMiscountOpen() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"More opening parentheses than closing ones: ((>1.4 && !=1.4.2)",
				() -> ModVersion.parsePredicate("((>1.4 && !=1.4.2)")
		);
	}

	@Test
	public void testParenthesesMiscountClose() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Closing/opening parenthesis mismatch: (>1.4 && !=1.4.2))",
				() -> ModVersion.parsePredicate("(>1.4 && !=1.4.2))")
		);
	}

	@Test
	public void testExtraBooleanOp() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Cannot merge boolean ops: even number of tokens: >1.4 && !=1.4.2 &&",
				() -> ModVersion.parsePredicate(">1.4 && !=1.4.2 &&")
		);
	}

	@Test
	public void testMissingBooleanOp() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Cannot merge boolean ops: even number of tokens: >1.4 !=1.4.2",
				() -> ModVersion.parsePredicate(">1.4 !=1.4.2")
		);
	}

	@Test
	public void testGarbage() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Expected operator before version at index 1: (dflfdn>===!=<,aazsd11!!",
				() -> ModVersion.parsePredicate("(dflfdn>===!=<,aazsd11!!")
		);
	}

	private static boolean test(Predicate<ModVersion> predicate, String version) {
		return predicate.test(ModVersion.of(version));
	}
}
