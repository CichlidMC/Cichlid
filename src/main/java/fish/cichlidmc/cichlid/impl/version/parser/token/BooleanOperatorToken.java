package fish.cichlidmc.cichlid.impl.version.parser.token;

public enum BooleanOperatorToken implements Token {
	AND("&&"),
	OR("||");

	private final String string;

	BooleanOperatorToken(String string) {
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
