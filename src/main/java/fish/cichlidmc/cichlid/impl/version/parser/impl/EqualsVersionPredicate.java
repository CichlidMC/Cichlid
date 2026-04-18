package fish.cichlidmc.cichlid.impl.version.parser.impl;

import fish.cichlidmc.cichlid.impl.version.parser.token.VersionOperatorToken;

import java.util.function.Predicate;

public record EqualsVersionPredicate<T>(T reference) implements Predicate<T> {
	@Override
	public boolean test(T version) {
		return this.reference.equals(version);
	}

	@Override
	public String toString() {
		return VersionOperatorToken.EQUAL.toString() + this.reference;
	}
}
