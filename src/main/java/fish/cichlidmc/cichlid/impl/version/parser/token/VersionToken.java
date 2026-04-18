package fish.cichlidmc.cichlid.impl.version.parser.token;

public record VersionToken(String version) implements Token {
	@Override
	public int length() {
		return this.version.length();
	}

	@Override
	public String toString() {
		return this.version;
	}
}
