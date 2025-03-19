package io.github.cichlidmc.cichlid.impl.version.parser;

import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.cichlid.impl.util.Either;
import io.github.cichlidmc.cichlid.impl.util.IntRange;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.cichlid.impl.version.parser.impl.AlwaysTrueVersionPredicate;
import io.github.cichlidmc.cichlid.impl.version.parser.impl.ParsedVersionPredicate;
import io.github.cichlidmc.cichlid.impl.version.parser.token.BooleanOperatorToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.ParenthesisToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.Token;
import io.github.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;
import io.github.cichlidmc.cichlid.impl.version.parser.token.VersionToken;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VersionPredicateParser {
	public static VersionPredicate parse(String string) {
		string = string.trim();

		if (string.equals("any")) {
			return AlwaysTrueVersionPredicate.INSTANCE;
		} else if (string.isEmpty()) {
			throw new VersionPredicateSyntaxException("Version predicate cannot be empty");
		}

		List<Either<VersionPredicate, Token>> tokens = VersionPredicateTokenizer.tokenize(string)
				.stream()
				.map(Either::<VersionPredicate, Token>right) // ew
				.collect(Collectors.toList());

		return parse(tokens, string);
	}

	private static VersionPredicate parse(List<Either<VersionPredicate, Token>> tokens, String string) {
		// fast path for most common case: operator and version
		if (tokens.size() == 2) {
			VersionPredicate predicate = matchSimplePattern(tokens, 0);
			if (predicate != null) {
				return predicate;
			}
		}

		List<Either<VersionPredicate, Token>> simplified = simplifyParentheses(tokens, string);
		if (anyMatch(simplified, token -> token instanceof ParenthesisToken)) {
			throw new RuntimeException("Failed to simplify out all parentheses");
		}

		List<Either<VersionPredicate, Token>> evaluated = evaluateSimplePredicates(simplified);
		if (anyMatch(evaluated, token -> token instanceof VersionOperatorToken || token instanceof VersionToken)) {
			throw new RuntimeException("failed to evaluate all simple predicates");
		}

		// now only contains parsed predicates and boolean operations
		List<Either<VersionPredicate, BooleanOperatorToken>> validated = assertFullySimplified(evaluated, string);
		// && has precedence, same as java
		List<Either<VersionPredicate, BooleanOperatorToken>> anded = evaluateBooleanOps(validated, BooleanOperatorToken.AND);
		List<Either<VersionPredicate, BooleanOperatorToken>> ored = evaluateBooleanOps(anded, BooleanOperatorToken.OR);
		if (ored.size() != 1 || ored.get(0).isRight()) {
			throw new RuntimeException("Something has gone very wrong");
		}

		return new ParsedVersionPredicate(ored.get(0).left(), string);
	}

	private static List<Either<VersionPredicate, Token>> simplifyParentheses(List<Either<VersionPredicate, Token>> tokens, String string) {
		// find top-level parentheses groups
		List<IntRange> groups = new ArrayList<>();
		int depth = 0;
		for (int i = 0, openIndex = -1; i < tokens.size(); i++) {
			Either<VersionPredicate, Token> either = tokens.get(i);
			if (either.isLeft())
				continue;

			Token token = either.right();
			if (token == ParenthesisToken.OPEN) {
				depth++;
				if (depth == 1) {
					openIndex = i;
				}
			} else if (token == ParenthesisToken.CLOSE) {
				depth--;
				if (depth < 0) {
					throw new VersionPredicateSyntaxException("Closing/opening parenthesis mismatch", string);
				} else if (depth == 0) {
					// completed group
					// openIndex can never be -1 here, if it was the depth < 0 would also be hit
					groups.add(new IntRange(openIndex, i));
					openIndex = -1;
				}
			}
		}

		if (depth > 0) {
			throw new VersionPredicateSyntaxException("More opening parentheses than closing ones", string);
		}

		if (groups.isEmpty())
			return tokens;

		// now go through tokens again, simplifying the groups
		List<Either<VersionPredicate, Token>> simplified = new ArrayList<>();
		List<Either<VersionPredicate, Token>> inCurrentGroup = new ArrayList<>();
		outer: for (int i = 0; i < tokens.size(); i++) {
			Either<VersionPredicate, Token> either = tokens.get(i);

			for (IntRange group : groups) {
				if (group.contains(i)) {
					if (i != group.min && i != group.max) {
						// discard the group's parentheses
						inCurrentGroup.add(either);
					}


					if (i == group.max) {
						// final token in group
						VersionPredicate predicate = parse(inCurrentGroup, string);
						simplified.add(Either.left(predicate));
						inCurrentGroup.clear();
					}

					// skip adding this token
					continue outer;
				}
			}

			// not in a group, don't touch it
			simplified.add(either);
		}

		return simplified;
	}

	private static List<Either<VersionPredicate, Token>> evaluateSimplePredicates(List<Either<VersionPredicate, Token>> tokens) {
		// replace all operations followed by a version with a simple predicate
		List<Either<VersionPredicate, Token>> evaluated = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			VersionPredicate predicate = matchSimplePattern(tokens, i);
			if (predicate != null) {
				evaluated.add(Either.left(predicate));
				i++; // skip second token
			} else {
				// didn't match, add it untouched
				evaluated.add(tokens.get(i));
			}
		}
		return evaluated;
	}

	private static List<Either<VersionPredicate, BooleanOperatorToken>> evaluateBooleanOps(List<Either<VersionPredicate, BooleanOperatorToken>> tokens, BooleanOperatorToken type) {
		if (tokens.size() == 1)
			return tokens;

		List<Either<VersionPredicate, BooleanOperatorToken>> evaluated = new ArrayList<>();
		for (int i = 1; i < tokens.size(); i += 2) {
			Either<VersionPredicate, BooleanOperatorToken> op = tokens.get(i);
			Either<VersionPredicate, BooleanOperatorToken> left = tokens.get(i - 1);
			Either<VersionPredicate, BooleanOperatorToken> right = tokens.get(i + 1);
			if (op.right() == type) {
				VersionPredicate merged = op.right().apply(left.left(), right.left());
				evaluated.add(Either.left(merged));
			} else {
				// doesn't match, just pass them through
				evaluated.add(left);
				evaluated.add(op);
				if (i + 2 >= tokens.size()) {
					// only add the right one when this is the last boolean op.
					// otherwise it might be consumed by the next op and get duplicated
					evaluated.add(right);
				}
			}
		}
		return evaluated;
	}

	private static VersionPredicate matchSimplePattern(List<Either<VersionPredicate, Token>> tokens, int i) {
		Either<VersionPredicate, Token> either = tokens.get(i);
		Either<VersionPredicate, Token> next = Utils.nextOrNull(tokens, i);
		if (either.isRight() && either.right() instanceof VersionOperatorToken && next != null && next.isRight() && next.right() instanceof VersionToken) {
			VersionOperatorToken operator = (VersionOperatorToken) either.right();
			VersionToken version = (VersionToken) next.right();
			return operator.createPredicate(version.version);
		}
		return null;
	}

	private static List<Either<VersionPredicate, BooleanOperatorToken>> assertFullySimplified(List<Either<VersionPredicate, Token>> tokens, String string) {
		if (tokens.size() % 2 == 0) {
			// must be an odd number of tokens
			throw new VersionPredicateSyntaxException("Cannot merge boolean ops: even number of tokens", string);
		}
		// predicate, boolean op, predicate, boolean op, etc
		for (int i = 0; i < tokens.size(); i++) {
			Either<VersionPredicate, Token> either = tokens.get(i);
			if (!isCorrectToken(i, either)) {
				throw new VersionPredicateSyntaxException("Cannot merge boolean ops: must alternate between predicates and ops", string);
			}
		}
		//noinspection unchecked
		return (List<Either<VersionPredicate, BooleanOperatorToken>>) (Object) tokens;
	}

	private static boolean isCorrectToken(int i, Either<VersionPredicate, Token> either) {
		if (i % 2 == 0) {
			// even, predicate expected
			return either.isLeft();
		} else {
			// odd, boolean op expected
			return either.isRight() && either.right() instanceof BooleanOperatorToken;
		}
	}

	private static boolean anyMatch(List<Either<VersionPredicate, Token>> tokens, Predicate<Token> predicate) {
		for (Either<VersionPredicate, Token> either : tokens) {
			if (either.isRight() && predicate.test(either.right())) {
				return true;
			}
		}
		return false;
	}
}
