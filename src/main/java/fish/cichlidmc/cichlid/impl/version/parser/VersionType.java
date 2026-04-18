package fish.cichlidmc.cichlid.impl.version.parser;

import fish.cichlidmc.cichlid.api.version.MinecraftVersion;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.impl.version.OptionalComparable;

import java.util.OptionalInt;

public interface VersionType<T> {
	T parse(String string);

	// only used to give a better error message during parsing, asComparable is the source of truth
	boolean isOnlyComparableByEquality(T version);

	OptionalComparable<T> asComparable(T version);

	enum Mod implements VersionType<ModVersion> {
		INSTANCE;

		@Override
		public ModVersion parse(String string) {
			return ModVersion.of(string);
		}

		@Override
		public boolean isOnlyComparableByEquality(ModVersion version) {
			return false;
		}

		@Override
		public OptionalComparable<ModVersion> asComparable(ModVersion version) {
			return OptionalComparable.of(version);
		}
	}

	enum Minecraft implements VersionType<MinecraftVersion> {
		INSTANCE;

		@Override
		public MinecraftVersion parse(String string) {
			return MinecraftVersion.parseAny(string);
		}

		@Override
		public boolean isOnlyComparableByEquality(MinecraftVersion version) {
			return version instanceof MinecraftVersion.Special;
		}

		@Override
		public OptionalComparable<MinecraftVersion> asComparable(MinecraftVersion version) {
			return that -> {
				if (version instanceof MinecraftVersion.Standard s1 && that instanceof MinecraftVersion.Standard s2) {
					return OptionalInt.of(s1.compareTo(s2));
				} else {
					return OptionalInt.empty();
				}
			};
		}
	}
}
