package fish.cichlidmc.cichlid.impl.version.parser.impl;

import fish.cichlidmc.cichlid.impl.version.parser.token.BooleanOperatorToken;

import java.util.function.Predicate;

public record BooleanOperatorPredicate<T>(Predicate<T> left, Predicate<T> right, BooleanOperatorToken operator) implements Predicate<T> {
	@Override
	public boolean test(T version) {
		return switch (this.operator) {
			case AND -> this.left.test(version) && this.right.test(version);
			case OR -> this.left.test(version) || this.right.test(version);
		};
	}

	@Override
	public String toString() {
		return "(%s %s %s)".formatted(this.left, this.operator, this.right);
	}
}
