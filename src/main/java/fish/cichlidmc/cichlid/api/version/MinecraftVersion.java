package fish.cichlidmc.cichlid.api.version;

import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.cichlid.impl.version.MinecraftVersionImpl;
import fish.cichlidmc.cichlid.impl.version.parser.VersionPredicateParser;
import fish.cichlidmc.cichlid.impl.version.parser.VersionType;

import java.util.Optional;
import java.util.function.Predicate;

/// A version of Minecraft.
/// @see ModVersion
public sealed interface MinecraftVersion {
	/// A predicate matching any version of Minecraft.
	/// Represented as a string with the value `any`.
	/// @see ModVersion#ANY_PREDICATE
	Predicate<MinecraftVersion> ANY_PREDICATE = parsePredicate("any");

	/// @return the currently loaded version of Minecraft
	static MinecraftVersion current() {
		return CichlidImpl.MINECRAFT_VERSION.get();
	}

	/// Attempt to parse a standard Minecraft version from the given string.
	static Optional<? extends Standard> parseStandard(String string) {
		return MinecraftVersionImpl.parseStandard(string);
	}

	/// Parse the given string into a Minecraft version.
	///
	/// First tries to parse the string into a [Standard] version.
	/// If that fails, the version is assumed to be a [Special] version.
	/// @throws IllegalArgumentException if the given string is blank
	static MinecraftVersion parseAny(String string) {
		Optional<? extends Standard> standard = parseStandard(string);
		return standard.isPresent() ? standard.get() : new Special(string);
	}

	/// Parse the given string into a predicate matching a set of versions.
	///
	/// This has the exact same behavior as the [ModVersion variant][ModVersion#parsePredicate(String)], with one exception:
	/// Comparisons against [Special][Special] versions can only check for equality.
	///
	/// This means that something like `==25w14craftmine` or `!=25w14craftmine` would be allowed, while `>=25w14craftmine` would not.
	///
	/// @throws VersionPredicateSyntaxException if the predicate is malformed
	/// @see ModVersion#parsePredicate(String)
	static Predicate<MinecraftVersion> parsePredicate(String string) throws VersionPredicateSyntaxException {
		return VersionPredicateParser.parse(VersionType.Minecraft.INSTANCE, string);
	}

	/// A standard Minecraft version, either a release or a non-release (snapshot, pre-release, or release candidate).
	sealed interface Standard extends MinecraftVersion, Comparable<Standard> {
		@Override
		default int compareTo(Standard that) {
			return MinecraftVersionImpl.compare(this, that);
		}
	}

	/// A released version of Minecraft, like `26.1.2` or `26.2`.
	/// When the patch is omitted, it's implied to be 0.
	record Release(int year, int drop, int patch) implements Standard {
		/// @throws IllegalArgumentException if any components are out of their allowed ranges
		public Release {
			if (year < 26) {
				throw new IllegalArgumentException("Year out of range: " + year);
			} else if (drop < 1) {
				throw new IllegalArgumentException("Drop Cannot be less than 1: " + drop);
			} else if (patch < 0) {
				throw new IllegalArgumentException("Patch cannot be negative: " + patch);
			}
		}

		@Override
		public String toString() {
			return this.patch == 0
					? this.year + "." + this.drop
					: this.year + "." + this.drop + "." + this.patch;
		}
	}

	/// A non-release version of Minecraft. Either a snapshot, pre-release, or release candidate. Some examples:
	/// - Snapshot: `26.2-snapshot-3`
	/// - Pre-release: `26.1-pre-1`
	/// - Release Candidate: `26.1.2-rc-1`
	/// @param release the release version this non-release is leading up to. For example, `26.2-snapshot-3` is for `26.2`.
	record NonRelease(Release release, int number, Type type) implements Standard {
		/// @throws IllegalArgumentException if the `number` is less than 1
		public NonRelease {
			if (number < 1) {
				throw new IllegalArgumentException("Number cannot be less than 1: " + number);
			}
		}

		@Override
		public String toString() {
			return this.release + "-" + this.type.versionString + '-' + this.number;
		}

		public enum Type {
			SNAPSHOT("snapshot"),
			PRE_RELEASE("pre"),
			RELEASE_CANDIDATE("rc");

			public final String versionString;

			Type(String versionString) {
				this.versionString = versionString;
			}
		}
	}

	/// A special version of Minecraft that does not use the standard format, such as the April Fools snapshots.
	/// These are completely disjoint from other Minecraft versions.
	record Special(String string) implements MinecraftVersion {
		public Special(String string) {
			this.string = string.trim();

			if (this.string.isEmpty()) {
				throw new IllegalArgumentException("Version cannot be empty");
			}
		}

		@Override
		public String toString() {
			return this.string;
		}
	}
}
