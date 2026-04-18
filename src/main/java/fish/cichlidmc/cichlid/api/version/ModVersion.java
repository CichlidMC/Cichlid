package fish.cichlidmc.cichlid.api.version;

import fish.cichlidmc.cichlid.impl.version.ModVersionImpl;
import fish.cichlidmc.cichlid.impl.version.parser.VersionPredicateParser;
import fish.cichlidmc.cichlid.impl.version.parser.VersionType;

import java.util.function.Predicate;

/// A Version of a mod.
///
/// This class implements [FlexVer](https://git.sleeping.town/unascribed/FlexVer)
/// (specifically version 1.1.1), a SemVer-compatible version format.
public sealed interface ModVersion extends Comparable<ModVersion> permits ModVersionImpl {
	/// Compare this version to another. See FlexVer's README for details.
	/// Comparing two versions of differing formats is valid, but probably won't make sense (garbage in, garbage out).
	@Override
	int compareTo(ModVersion that);

	/// Check if this Version is exactly equal to another.
	///
	/// **Beware:** this checks that the strings of the two versions are exactly equal.
	/// Versions that are logically equivalent may have different strings, and therefore not be equal.
	///
	/// You most likely want `v1.compareTo(v2) == 0` instead.
	@Override
	boolean equals(Object obj);

	/// Compute a hashcode for this Version.
	///
	/// **Beware:** this simply defers to the string form of this version.
	/// This means that two versions may be logically equivalent, but have different hash codes.
	///
	/// Note that the contract between `equals` and `hashCode` is still maintained;
	/// `equals` also operates on the string form.
	@Override
	int hashCode();

	/// Create a version from the given string.
	/// Any non-empty string will produce a valid Version.
	/// @throws IllegalArgumentException if the given string is blank
	static ModVersion of(String string) {
		return ModVersionImpl.of(string);
	}

	/// Parse the given string into a predicate matching a set of versions. Syntax is as follows:
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
	/// When [Object#toString()] is called on the returned predicate, it will
	/// return a predicate string equivalent to the one that was originally parsed.
	/// @throws VersionPredicateSyntaxException if the predicate is malformed
	/// @see MinecraftVersion#parsePredicate(String)
	static Predicate<ModVersion> parsePredicate(String string) throws VersionPredicateSyntaxException {
		return VersionPredicateParser.parse(VersionType.Mod.INSTANCE, string);
	}
}
