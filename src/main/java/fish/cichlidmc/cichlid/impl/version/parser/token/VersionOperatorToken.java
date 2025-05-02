package fish.cichlidmc.cichlid.impl.version.parser.token;

import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.api.version.VersionPredicate;

import java.util.function.Function;

public enum VersionOperatorToken implements Token {
	LESS_OR_EQUAL("<=", reference -> version -> version.compareTo(reference) <= 0),
	GREATER_OR_EQUAL(">=", reference -> version -> version.compareTo(reference) >= 0),
	EQUAL("==", reference -> version -> version.compareTo(reference) == 0),
	NOT_EQUAL("!=", reference -> version -> version.compareTo(reference) != 0),
	LESS_THAN("<", reference -> version -> version.compareTo(reference) < 0),
	GREATER_THAN(">", reference -> version -> version.compareTo(reference) > 0);

	private final String string;
	private final Function<Version, VersionPredicate> predicateFactory;

	VersionOperatorToken(String string, Function<Version, VersionPredicate> predicateFactory) {
		this.string = string;
		this.predicateFactory = predicateFactory;
	}

	public VersionPredicate createPredicate(Version version) {
		return this.predicateFactory.apply(version);
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
