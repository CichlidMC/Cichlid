package fish.cichlidmc.cichlid.impl.version.parser.impl;

import fish.cichlidmc.cichlid.impl.version.OptionalComparable;
import fish.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;

import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public record VersionOperatorPredicate<T>(T reference, VersionOperatorToken operator, OptionalComparable<T> comparable) implements Predicate<T> {
	@Override
	public boolean test(T version) {
		return switch (this.operator) {
			case LESS_OR_EQUAL -> this.test(version, i -> i <= 0);
			case GREATER_OR_EQUAL -> this.test(version, i -> i >= 0);
			case EQUAL -> this.test(version, i -> i == 0);
			case LESS_THAN -> this.test(version, i -> i < 0);
			case GREATER_THAN -> this.test(version, i -> i > 0);
			case NOT_EQUAL -> {
				// special case: if two versions are incomparable, they're not equal
				OptionalInt result = this.compare(version);
				yield result.isEmpty() || result.getAsInt() != 0;
			}
		};
	}

	@Override
	public String toString() {
		return this.operator.toString() + this.reference;
	}

	private boolean test(T value, IntPredicate test) {
		OptionalInt result = this.compare(value);
		if (result.isEmpty())
			return false;

		return test.test(result.getAsInt());
	}

	private OptionalInt compare(T value) {
		OptionalInt result = this.comparable.compareTo(value);
		return result.isEmpty() ? result : OptionalInt.of(-result.getAsInt());
	}
}
