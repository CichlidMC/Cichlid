package fish.cichlidmc.cichlid.api.version;

/**
 * Exception possibly thrown when parsing a version predicate.
 */
public class VersionPredicateSyntaxException extends RuntimeException {
	public VersionPredicateSyntaxException(String message) {
		super(message);
	}

	public VersionPredicateSyntaxException(String message, String predicate) {
		super(message + ": " + predicate);
	}
}
