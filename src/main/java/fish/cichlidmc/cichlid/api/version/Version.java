package fish.cichlidmc.cichlid.api.version;

import fish.cichlidmc.cichlid.impl.version.VersionImpl;

/**
 * A Version of a mod, plugin, Minecraft, Cichlid, or anything really.
 * <p>
 * This class implements <a href="https://git.sleeping.town/unascribed/FlexVer">FlexVer</a>
 * (specifically version 1.1.1), a SemVer-compatible version format.
 */
public sealed interface Version extends Comparable<Version> permits VersionImpl {
	/**
	 * @return the string that this version was parsed from
	 */
	@Override
	String toString();

	/**
	 * Compare this version to another. See FlexVer's README for details.
	 * Comparing two versions of differing formats is valid, but probably won't make sense (garbage in, garbage out).
	 */
	@Override
	int compareTo(Version that);

	/**
	 * Check if this Version is exactly equal to another.
	 * <p>
	 * <strong>Beware:</strong> this checks that the strings of the two versions are exactly equal.
	 * Versions that are logically equivalent may have different strings, and therefore not be equal.
	 * <p>
	 * You most likely want {@code v1.compareTo(v2) == 0} instead.
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Compute a hashcode for this Version.
	 * <p>
	 * <strong>Beware:</strong> this simply defers to the string form of this version.
	 * This means that two versions may be logically equivalent, but have different hash codes.
	 * <p>
	 * Note that the contract between {@code equals} and {@code hashCode} is still maintained;
	 * {@code equals} also operates on the string form.
	 */
	@Override
	int hashCode();

	/**
	 * Create a version from the given string.
	 * Creating a version will never fail, and will always return a valid Version.
	 */
	static Version of(String string) {
		return VersionImpl.of(string);
	}
}
