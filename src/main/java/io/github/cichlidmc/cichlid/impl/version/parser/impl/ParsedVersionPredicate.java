package io.github.cichlidmc.cichlid.impl.version.parser.impl;

import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;

public final class ParsedVersionPredicate implements VersionPredicate {
	private final VersionPredicate wrapped;
	private final String string;

	public ParsedVersionPredicate(VersionPredicate wrapped, String string) {
		this.wrapped = wrapped;
		this.string = string;
	}

	@Override
	public boolean test(Version version) {
		return this.wrapped.test(version);
	}

	@Override
	public String toString() {
		return this.string;
	}
}
