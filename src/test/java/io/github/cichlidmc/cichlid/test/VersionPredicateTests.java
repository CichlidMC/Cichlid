package io.github.cichlidmc.cichlid.test;

import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import org.junit.jupiter.api.Test;

import static io.github.cichlidmc.cichlid.test.MoreAssertions.assertThrowsWithMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionPredicateTests {
	@Test
	public void testLe() {
		VersionPredicate predicate = VersionPredicate.parse("<=1.20");
		assertTrue(predicate.test("1.19"));
		assertTrue(predicate.test("1.20"));
		assertTrue(predicate.test("1.20-beta.1"));
		assertTrue(predicate.test("1.20-pre1"));
		assertTrue(predicate.test("1.20-a"));
		assertFalse(predicate.test("1.20.1"));
	}

	@Test
	public void testGe() {
		VersionPredicate predicate = VersionPredicate.parse(">=1.12.2");
		assertFalse(predicate.test("1.12.2-beta.5"));
		assertFalse(predicate.test("1.12.1.1.1"));
		assertFalse(predicate.test("1.12.1+build.100"));
		assertTrue(predicate.test("1.12.2"));
		assertTrue(predicate.test("1.12.2.1"));
		assertTrue(predicate.test("1.13.1"));
		assertTrue(predicate.test("1.12.2e"));
	}

	@Test
	public void testEqual() {
		VersionPredicate predicate = VersionPredicate.parse(" ==  1.12.2");
		assertFalse(predicate.test("1.12.2-beta.5"));
		assertFalse(predicate.test("1.12.1.1.1"));
		assertFalse(predicate.test("1.12.1+build.100"));
		assertTrue(predicate.test("1.12.2"));
		assertFalse(predicate.test("1.12.2.1"));
		assertFalse(predicate.test("1.13.1"));
		assertFalse(predicate.test("1.12.2e"));
	}

	@Test
	public void testNotEqual() {
		VersionPredicate predicate = VersionPredicate.parse("!= 1.12.2  ");
		assertTrue(predicate.test("1.12.2-beta.5"));
		assertTrue(predicate.test("1.12.1.1.1"));
		assertTrue(predicate.test("1.12.1+build.100"));
		assertFalse(predicate.test("1.12.2"));
		assertTrue(predicate.test("1.12.2.1"));
		assertTrue(predicate.test("1.13.1"));
		assertTrue(predicate.test("1.12.2e"));
	}

	@Test
	public void testLt() {
		VersionPredicate predicate = VersionPredicate.parse("<1.12.2");
		assertTrue(predicate.test("1.12.2-beta.5"));
		assertTrue(predicate.test("1.12.1.1.1"));
		assertTrue(predicate.test("1.12.1+build.100"));
		assertFalse(predicate.test("1.12.2"));
		assertFalse(predicate.test("1.12.2.1"));
		assertFalse(predicate.test("1.13.1"));
		assertFalse(predicate.test("1.12.2e"));
	}

	@Test
	public void testGt() {
		VersionPredicate predicate = VersionPredicate.parse("> 1.12.2");
		assertFalse(predicate.test("1.12.2-beta.5"));
		assertFalse(predicate.test("1.12.1.1.1"));
		assertFalse(predicate.test("1.12.1+build.100"));
		assertFalse(predicate.test("1.12.2"));
		assertTrue(predicate.test("1.12.2.1"));
		assertTrue(predicate.test("1.13.1"));
		assertTrue(predicate.test("1.12.2e"));
	}

	@Test
	public void testAny() {
		VersionPredicate predicate = VersionPredicate.parse("any ");
		assertTrue(predicate.test("1.12"));
		assertTrue(predicate.test("1.9.4"));
		assertTrue(predicate.test("1.3.1.2-build.5"));
		assertTrue(predicate.test("22w35a"));
		assertTrue(predicate.test("aaaaaa"));
	}

	@Test
	public void testCreateLike() {
		VersionPredicate predicate = VersionPredicate.parse(">=0.5.1.i");
		assertTrue(predicate.test("0.5.1.i"));
		assertTrue(predicate.test("0.5.1.j"));
		assertTrue(predicate.test("0.5.2"));
		assertTrue(predicate.test("0.5.2.a"));
		assertFalse(predicate.test("0.5.0"));
		assertFalse(predicate.test("0.5.1.h"));
	}

	@Test
	public void testCreateFabricLike() {
		VersionPredicate predicate = VersionPredicate.parse(">=0.5.1-i");
		assertTrue(predicate.test("0.5.1-i-build.1000+mc1.20"));
		assertTrue(predicate.test("0.5.1-j"));
		assertTrue(predicate.test("0.5.2-a"));
		assertTrue(predicate.test("0.5.2-i"));
		assertFalse(predicate.test("0.5.0-i"));
		assertFalse(predicate.test("0.5.1-h"));
	}

	@Test
	public void testSnapshot() {
		VersionPredicate predicate = VersionPredicate.parse("<=24w25a");
		assertTrue(predicate.test("23w25a"));
		assertTrue(predicate.test("24w23a"));
		assertFalse(predicate.test("25w12a"));
		assertFalse(predicate.test("24w30a"));
		assertFalse(predicate.test("24w25b"));
	}

	@Test
	public void testNested() {
		VersionPredicate predicate = VersionPredicate.parse("(((((>=1.20)))))");
		assertTrue(predicate.test("1.20"));
		assertTrue(predicate.test("1.21"));
		assertFalse(predicate.test("1.19"));
	}

	@Test
	public void testAnd() {
		VersionPredicate predicate = VersionPredicate.parse(">=1.16 && <1.17");
		assertTrue(predicate.test("1.16"));
		assertTrue(predicate.test("1.16.5"));
		assertTrue(predicate.test("1.16.3"));
		assertFalse(predicate.test("1.17"));
		assertTrue(predicate.test("1.17-alpha.1"));
	}

	@Test
	public void testOr() {
		VersionPredicate predicate = VersionPredicate.parse(">=1.16 || ==1.14.2");
		assertTrue(predicate.test("1.16"));
		assertTrue(predicate.test("1.16.5"));
		assertFalse(predicate.test("1.14"));
		assertFalse(predicate.test("1.14.1"));
		assertTrue(predicate.test("1.14.2"));
	}

	@Test
	public void testComplex() {
		VersionPredicate predicate = VersionPredicate.parse("(>=1.14 && (<= 1.14.4 || ==1.15.2)) || >22w13a &&<22w13c");
		assertTrue(predicate.test("1.14"));
		assertTrue(predicate.test("1.14.2"));
		assertTrue(predicate.test("1.14.4"));
		assertFalse(predicate.test("1.15.1"));
		assertTrue(predicate.test("1.15.2"));
		assertFalse(predicate.test("22w13a"));
		assertTrue(predicate.test("22w13b"));
		assertFalse(predicate.test("22w13c"));
	}

	@Test
	public void testPrecedence() {
		// if the grouping is (!=22w35a || >23w15b) && <23w17c, then it simplifies to !=22w35a && <23w17c (wrong)
		// if the grouping is !=22w35a || (>23w15b && <23w17c), then it simplifies to !=22w35a (right)
		VersionPredicate predicate = VersionPredicate.parse("!=22w35a || >23w15b && <23w17c");
		assertTrue(predicate.test("12w01a"));
		assertTrue(predicate.test("99w52z"));
		assertFalse(predicate.test("22w35a"));
	}

	@Test
	public void testEmpty() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Version predicate cannot be empty",
				() -> VersionPredicate.parse("   ")
		);
	}

	@Test
	public void testExtraOpeningParenthesis() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"More opening parentheses than closing ones: (>1.4 && !=1.4.2) || (==22w14a",
				() -> VersionPredicate.parse("(>1.4 && !=1.4.2) || (==22w14a")
		);
	}

	@Test
	public void testSwappedParentheses() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Closing/opening parenthesis mismatch: )>1.4 && !=1.4.2(",
				() -> VersionPredicate.parse(")>1.4 && !=1.4.2(")
		);
	}

	@Test
	public void testParenthesesMiscountOpen() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"More opening parentheses than closing ones: ((>1.4 && !=1.4.2)",
				() -> VersionPredicate.parse("((>1.4 && !=1.4.2)")
		);
	}

	@Test
	public void testParenthesesMiscountClose() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Closing/opening parenthesis mismatch: (>1.4 && !=1.4.2))",
				() -> VersionPredicate.parse("(>1.4 && !=1.4.2))")
		);
	}

	@Test
	public void testExtraBooleanOp() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Cannot merge boolean ops: even number of tokens: >1.4 && !=1.4.2 &&",
				() -> VersionPredicate.parse(">1.4 && !=1.4.2 &&")
		);
	}

	@Test
	public void testMissingBooleanOp() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Cannot merge boolean ops: even number of tokens: >1.4 !=1.4.2",
				() -> VersionPredicate.parse(">1.4 !=1.4.2")
		);
	}

	@Test
	public void testGarbage() {
		assertThrowsWithMessage(
				VersionPredicateSyntaxException.class,
				"Expected operator before version at index 1: (dflfdn>===!=<,aazsd11!!",
				() -> VersionPredicate.parse("(dflfdn>===!=<,aazsd11!!")
		);
	}
}
