package fish.cichlidmc.cichlid.impl.version.parser.token;

public enum ParenthesisToken implements Token {
	OPEN("("), CLOSE(")");

	private final String string;

	ParenthesisToken(String string) {
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
