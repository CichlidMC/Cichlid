package io.github.cichlidmc.cichlid.api.version;

import io.github.cichlidmc.cichlid.impl.version.VersionImpl;
import org.jetbrains.annotations.NotNull;

/**
 * A Version of a mod, plugin, Minecraft, or Cichlid itself.
 * <p>
 * Cichlid versions use FlexVer (specifically version 1.1.1), a SemVer-compatible version format.
 * For more information, see <a href="https://github.com/unascribed/FlexVer">here</a>.
 */
public interface Version extends Comparable<Version> {
	/**
	 * Returns the same string that this version was parsed from.
	 */
	@Override
	String toString();

	/**
	 * Compare this version to another.
	 * See <a href="https://github.com/unascribed/FlexVer">the FlexVer GitHub page</a> for specifics on how this is done.
	 * Comparing two versions of differing formats is valid, but probably won't make sense (garbage in, garbage out).
	 */
	@Override
	int compareTo(@NotNull Version o);

	/**
	 * Create a version from the given string.
	 * Creating a version will never fail, and will always return a valid Version.
	 */
	static Version of(String string) {
		return VersionImpl.of(string);
	}
}
