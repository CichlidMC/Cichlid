package fish.cichlidmc.cichlid.impl.version.parser;

import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import fish.cichlidmc.cichlid.impl.util.Either;
import fish.cichlidmc.cichlid.impl.util.IntRange;
import fish.cichlidmc.cichlid.impl.version.OptionalComparable;
import fish.cichlidmc.cichlid.impl.version.parser.impl.AnyVersionPredicate;
import fish.cichlidmc.cichlid.impl.version.parser.impl.BooleanOperatorPredicate;
import fish.cichlidmc.cichlid.impl.version.parser.impl.EqualsVersionPredicate;
import fish.cichlidmc.cichlid.impl.version.parser.impl.NotEqualsVersionPredicate;
import fish.cichlidmc.cichlid.impl.version.parser.impl.VersionOperatorPredicate;
import fish.cichlidmc.cichlid.impl.version.parser.token.BooleanOperatorToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.ParenthesisToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.Token;
import fish.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;
import fish.cichlidmc.cichlid.impl.version.parser.token.VersionToken;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class VersionPredicateParser<T> {
	private final VersionType<T> type;
	private final String string;

	private VersionPredicateParser(VersionType<T> type, String string) throws VersionPredicateSyntaxException {
		if (string.isBlank()) {
			throw VersionPredicateSyntaxException.ofEmpty("Version predicate cannot be empty");
		}

		this.type = type;
		this.string = string.trim();
	}

	public static <T> Predicate<T> parse(VersionType<T> type, String string) throws VersionPredicateSyntaxException {
		return new VersionPredicateParser<>(type, string).parse();
	}

	private Predicate<T> parse() throws VersionPredicateSyntaxException {
		if (this.string.equals("any")) {
			return AnyVersionPredicate.INSTANCE.cast();
		}

		List<Either<Predicate<T>, Token>> tokens = new ArrayList<>();
		VersionPredicateTokenizer.tokenize(this.string).forEach(token -> tokens.add(Either.right(token)));
		return this.parse(Collections.unmodifiableList(tokens));
	}

	private Predicate<T> parse(List<Either<Predicate<T>, Token>> tokens) {
		// fast path for most common case: operator and version
		if (tokens.size() == 2) {
			Predicate<T> predicate = this.matchSimplePattern(tokens, 0);
			if (predicate != null) {
				return predicate;
			}
		}

		List<Either<Predicate<T>, Token>> simplified = this.simplifyParentheses(tokens);
		if (anyRightsMatch(simplified, token -> token instanceof ParenthesisToken)) {
			throw this.fail("Failed to simplify out all parentheses");
		}

		List<Either<Predicate<T>, Token>> evaluated = this.evaluateSimplePredicates(simplified);
		if (anyRightsMatch(evaluated, token -> token instanceof VersionOperatorToken || token instanceof VersionToken)) {
			throw this.fail("failed to evaluate all simple predicates");
		}

		// now only contains parsed predicates and boolean operations
		List<Either<Predicate<T>, BooleanOperatorToken>> validated = this.assertFullySimplified(evaluated);
		// && has precedence, same as java
		List<Either<Predicate<T>, BooleanOperatorToken>> anded = this.evaluateBooleanOps(validated, BooleanOperatorToken.AND);
		List<Either<Predicate<T>, BooleanOperatorToken>> ored = this.evaluateBooleanOps(anded, BooleanOperatorToken.OR);
		if (ored.size() != 1 || ored.getFirst().isRight()) {
			throw this.fail("Something has gone very wrong");
		}

		return ored.getFirst().left();
	}

	private List<Either<Predicate<T>, Token>> simplifyParentheses(List<Either<Predicate<T>, Token>> tokens) {
		// find top-level parentheses groups
		List<IntRange> groups = new ArrayList<>();
		int depth = 0;
		for (int i = 0, openIndex = -1; i < tokens.size(); i++) {
			Either<Predicate<T>, Token> either = tokens.get(i);
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
					throw this.fail("Closing/opening parenthesis mismatch");
				} else if (depth == 0) {
					// completed group
					// openIndex can never be -1 here, if it was the depth < 0 would also be hit
					groups.add(new IntRange(openIndex, i));
					openIndex = -1;
				}
			}
		}

		if (depth > 0) {
			throw this.fail("More opening parentheses than closing ones");
		}

		if (groups.isEmpty())
			return tokens;

		// now go through tokens again, simplifying the groups
		List<Either<Predicate<T>, Token>> simplified = new ArrayList<>();
		List<Either<Predicate<T>, Token>> inCurrentGroup = new ArrayList<>();
		outer: for (int i = 0; i < tokens.size(); i++) {
			Either<Predicate<T>, Token> either = tokens.get(i);

			for (IntRange group : groups) {
				if (group.contains(i)) {
					if (i != group.min && i != group.max) {
						// discard the group's parentheses
						inCurrentGroup.add(either);
					}


					if (i == group.max) {
						// final token in group
						Predicate<T> predicate = this.parse(inCurrentGroup);
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

	private List<Either<Predicate<T>, Token>> evaluateSimplePredicates(List<Either<Predicate<T>, Token>> tokens) {
		// replace all operations followed by a version with a simple predicate
		List<Either<Predicate<T>, Token>> evaluated = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			Predicate<T> predicate = this.matchSimplePattern(tokens, i);
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

	private List<Either<Predicate<T>, BooleanOperatorToken>> evaluateBooleanOps(List<Either<Predicate<T>, BooleanOperatorToken>> tokens, BooleanOperatorToken type) {
		if (tokens.size() == 1)
			return tokens;

		List<Either<Predicate<T>, BooleanOperatorToken>> evaluated = new ArrayList<>();
		for (int i = 1; i < tokens.size(); i += 2) {
			Either<Predicate<T>, BooleanOperatorToken> op = tokens.get(i);
			Either<Predicate<T>, BooleanOperatorToken> left = tokens.get(i - 1);
			Either<Predicate<T>, BooleanOperatorToken> right = tokens.get(i + 1);
			if (op.right() == type) {
				Predicate<T> merged = new BooleanOperatorPredicate<>(left.left(), right.left(), op.right());
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

	@Nullable
	private Predicate<T> matchSimplePattern(List<Either<Predicate<T>, Token>> tokens, int i) {
		Either<Predicate<T>, Token> either = tokens.get(i);
		if (either.isLeft() || !(either.right() instanceof VersionOperatorToken operator))
			return null;

		Either<Predicate<T>, Token> next = nextOrNull(tokens, i);
		if (next == null || next.isLeft() || !(next.right() instanceof VersionToken(String versionString)))
			return null;

		T version = this.type.parse(versionString);

		if (this.type.isOnlyComparableByEquality(version)) {
			return switch (operator) {
				case EQUAL -> new EqualsVersionPredicate<>(version);
				case NOT_EQUAL -> new NotEqualsVersionPredicate<>(version);
				default -> throw this.fail("Version '" + version + "' can only be compared by equality (== or !=)");
			};
		}

		OptionalComparable<T> comparable = this.type.asComparable(version);
		return new VersionOperatorPredicate<>(version, operator, comparable);
	}

	@SuppressWarnings("unchecked") // cast at end is validated by loop
	private List<Either<Predicate<T>, BooleanOperatorToken>> assertFullySimplified(List<Either<Predicate<T>, Token>> tokens) {
		if (tokens.size() % 2 == 0) {
			// must be an odd number of tokens
			throw this.fail("Cannot merge boolean ops: even number of tokens");
		}

		// predicate, boolean op, predicate, boolean op, etc
		for (int i = 0; i < tokens.size(); i++) {
			Either<Predicate<T>, Token> either = tokens.get(i);
			if (!isExpectedSimplifiedToken(i, either)) {
				throw this.fail("Cannot merge boolean ops: must alternate between predicates and ops");
			}
		}

		return (List<Either<Predicate<T>, BooleanOperatorToken>>) (Object) tokens;
	}

	private VersionPredicateSyntaxException fail(String message) {
		return VersionPredicateSyntaxException.of(message, this.string);
	}

	private static boolean isExpectedSimplifiedToken(int i, Either<?, Token> either) {
		if (i % 2 == 0) {
			// even, predicate expected
			return either.isLeft();
		} else {
			// odd, boolean op expected
			return either.isRight() && either.right() instanceof BooleanOperatorToken;
		}
	}

	private static <L, R> boolean anyRightsMatch(List<Either<L, R>> tokens, Predicate<R> predicate) {
		for (Either<L, R> either : tokens) {
			if (either.isRight() && predicate.test(either.right())) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static <T> T nextOrNull(List<T> list, int i) {
		int next = i + 1;
		return next == list.size() ? null : list.get(next);
	}
}
