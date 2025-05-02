package fish.cichlidmc.cichlid.impl.version.parser.impl;

import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.api.version.VersionPredicate;

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
