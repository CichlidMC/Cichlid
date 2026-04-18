package fish.cichlidmc.cichlid.impl.version.parser.token;

public enum VersionOperatorToken implements Token {
	LESS_OR_EQUAL("<="),
	GREATER_OR_EQUAL(">="),
	EQUAL("=="),
	NOT_EQUAL("!="),
	LESS_THAN("<"),
	GREATER_THAN(">");

	private final String string;

	VersionOperatorToken(String string) {
		this.string = string;
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
