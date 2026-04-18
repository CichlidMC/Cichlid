package fish.cichlidmc.cichlid.api.version;

/// Exception possibly thrown when parsing a version predicate.
public final class VersionPredicateSyntaxException extends RuntimeException {
	/// The string that couldn't be parsed into a predicate.
	public final String predicate;

	private VersionPredicateSyntaxException(String message, String predicate) {
		super(message);
		this.predicate = predicate;
	}

	public static VersionPredicateSyntaxException ofEmpty(String message) {
		return new VersionPredicateSyntaxException(message, "");
	}

	public static VersionPredicateSyntaxException of(String message, String predicate) {
		return new VersionPredicateSyntaxException(message + ": " + predicate, predicate);
	}
}
