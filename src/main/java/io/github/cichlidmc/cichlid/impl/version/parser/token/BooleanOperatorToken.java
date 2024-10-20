package io.github.cichlidmc.cichlid.impl.version.parser.token;

import java.util.function.BiFunction;

import io.github.cichlidmc.cichlid.api.version.VersionPredicate;

public enum BooleanOperatorToken implements Token {
	AND("&&", (left, right) -> version -> left.test(version) && right.test(version)),
	OR("||", (left, right) -> version -> left.test(version) || right.test(version));

	private final String string;
	private final BiFunction<VersionPredicate, VersionPredicate, VersionPredicate> merger;

	BooleanOperatorToken(String string, BiFunction<VersionPredicate, VersionPredicate, VersionPredicate> merger) {
		this.string = string;
		this.merger = merger;
	}

	public VersionPredicate apply(VersionPredicate left, VersionPredicate right) {
		return this.merger.apply(left, right);
	}

	@Override
	public int length() {
		return this.string.length();
	}

	@Override
	public String toString() {
		return this.string;
	}
}
