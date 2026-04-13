package fish.cichlidmc.cichlid.impl.version.parser.impl;

import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.api.version.VersionPredicate;

public record ParsedVersionPredicate(VersionPredicate wrapped, String string) implements VersionPredicate {
	@Override
	public boolean test(Version version) {
		return this.wrapped.test(version);
	}

	@Override
	public String toString() {
		return this.string;
	}
}
