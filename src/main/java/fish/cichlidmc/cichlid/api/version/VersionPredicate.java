package fish.cichlidmc.cichlid.api.version;

import fish.cichlidmc.cichlid.impl.version.parser.VersionPredicateParser;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/// A predicate for [Version]s.
public interface VersionPredicate extends Predicate<Version> {
	/// Shortcut that parses a String into a Version for you.
	@ApiStatus.NonExtendable
	default boolean test(String version) {
		return this.test(Version.of(version));
	}

	/// Version predicates parsed from [#parse(String)] will return their original string.
	/// Other implementations have no such guarantee.
	@Override
	String toString();

	/// Parse a version predicate from the given string. Syntax is as follows:
	///   - Version operators: `<=, >=, ==, !=, <, >`
	///   - Boolean operators: `&&, ||`, same precedence as Java (and, then or)
	///   - Parentheses: May be used to explicitly group boolean operations
	///   - Versions: Any string after a version operator until parentheses or a boolean op are hit.
	///   - Whitespace: All whitespace is ignored.
	///   - Special case: the string `any` will parse into a predicate matching any version.
	///
	/// Examples:
	///   - `>=1.21.1`
	///   - `>=1.20 && <= 1.21`
	///   - `(>= 1.16 && <1.17) && !=1.16.3`
	///   - `>=24w10a && <24w12a`
	///   - `(>=0.5.1.a && <0.5.1.d) || >=0.5.1.f`
	///   - `==0.6.0-beta.2`
	///
	/// @throws SyntaxException if the predicate is malformed
	static VersionPredicate parse(String string) throws SyntaxException {
		return VersionPredicateParser.parse(string);
	}

	/// Exception possibly thrown when parsing a version predicate.
	final class SyntaxException extends RuntimeException {
		/// The String that failed to be parsed into a [VersionPredicate].
		public final String predicate;

		private SyntaxException(String message, String predicate) {
			super(message);
			this.predicate = predicate;
		}

		public static SyntaxException ofEmpty(String message) {
			return new SyntaxException(message, "");
		}

		public static SyntaxException of(String message, String predicate) {
			return new SyntaxException(message + ": " + predicate, predicate);
		}
	}
}
