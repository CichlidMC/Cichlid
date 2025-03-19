package io.github.cichlidmc.cichlid.impl.version.parser.impl;

import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;

public enum AlwaysTrueVersionPredicate implements VersionPredicate {
	INSTANCE;

	@Override
	public boolean test(Version version) {
		return true;
	}

	@Override
	public String toString() {
		return "any";
	}
}
