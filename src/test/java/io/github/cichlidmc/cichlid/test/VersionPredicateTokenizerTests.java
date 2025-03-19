package io.github.cichlidmc.cichlid.test;

import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.cichlid.impl.version.parser.VersionPredicateTokenizer;
import io.github.cichlidmc.cichlid.impl.version.parser.token.BooleanOperatorToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.ParenthesisToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.Token;
import io.github.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.VersionToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class VersionPredicateTokenizerTests {
	@Test
	public void testSimple() {
		List<Token> expected = Utils.listOf(VersionOperatorToken.EQUAL, new VersionToken("1.20"));
		List<Token> actual = VersionPredicateTokenizer.tokenize("==1.20");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testOperator() {
		List<Token> expected = Utils.listOf(VersionOperatorToken.GREATER_OR_EQUAL, new VersionToken("0.5e"));
		List<Token> actual = VersionPredicateTokenizer.tokenize(">=0.5e");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testWhitespace() {
		List<Token> expected = Utils.listOf(VersionOperatorToken.GREATER_OR_EQUAL, new VersionToken("0.5e"));
		List<Token> actual = VersionPredicateTokenizer.tokenize(">= 0.5e");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testTrailingWhitespace() {
		List<Token> expected = Utils.listOf(VersionOperatorToken.GREATER_OR_EQUAL, new VersionToken("0.5e"));
		List<Token> actual = VersionPredicateTokenizer.tokenize(">=0.5e    ");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testParenthesis() {
		List<Token> expected = Utils.listOf(
				ParenthesisToken.OPEN, VersionOperatorToken.LESS_OR_EQUAL, new VersionToken("1.0+1.21"), ParenthesisToken.CLOSE
		);
		List<Token> actual = VersionPredicateTokenizer.tokenize("(<=1.0+1.21)");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testTooManyParenthesis() {
		List<Token> expected = Utils.listOf(
				ParenthesisToken.OPEN, ParenthesisToken.OPEN, ParenthesisToken.OPEN, ParenthesisToken.OPEN,
				VersionOperatorToken.LESS_OR_EQUAL, new VersionToken("1.0+1.21"),
				ParenthesisToken.CLOSE, ParenthesisToken.CLOSE, ParenthesisToken.CLOSE
		);
		List<Token> actual = VersionPredicateTokenizer.tokenize("((( (<=1.0+1.21 ))  )");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testBooleanAnd() {
		List<Token> expected = Utils.listOf(
				VersionOperatorToken.GREATER_THAN, new VersionToken("1.3"),
				BooleanOperatorToken.AND,
				VersionOperatorToken.NOT_EQUAL, new VersionToken("1.3.2")
		);
		List<Token> actual = VersionPredicateTokenizer.tokenize(">1.3 && !=1.3.2");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testBooleanOr() {
		List<Token> expected = Utils.listOf(
				VersionOperatorToken.GREATER_THAN, new VersionToken("1.3"),
				BooleanOperatorToken.OR,
				VersionOperatorToken.NOT_EQUAL, new VersionToken("1.3.2")
		);
		List<Token> actual = VersionPredicateTokenizer.tokenize(">1.3 || !=1.3.2");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testBooleanComplex() {
		List<Token> expected = Utils.listOf(
				ParenthesisToken.OPEN,
				VersionOperatorToken.GREATER_THAN, new VersionToken("1.3"),
				BooleanOperatorToken.OR,
				VersionOperatorToken.NOT_EQUAL, new VersionToken("1.3.2"),
				BooleanOperatorToken.AND,
				VersionOperatorToken.LESS_THAN, new VersionToken("1.4"),
				ParenthesisToken.CLOSE,
				BooleanOperatorToken.OR,
				VersionOperatorToken.EQUAL, new VersionToken("3.0")
		);
		List<Token> actual = VersionPredicateTokenizer.tokenize("(>1.3 || !=1.3.2 && <1.4) || ==3.0");
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testUnknownOperator() {
		Assertions.assertThrows(VersionPredicateSyntaxException.class, () -> VersionPredicateTokenizer.tokenize("%1.5"));
	}
}
