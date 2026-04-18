package fish.cichlidmc.cichlid.impl.version;

import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.impl.version.FlexVerComparator.VersionComponent;

import java.util.List;

public record ModVersionImpl(String string, List<VersionComponent> components) implements ModVersion {
	@Override
	public int compareTo(ModVersion that) {
		return FlexVerComparator.compare(this.components, ((ModVersionImpl) that).components);
	}

	@Override
	public String toString() {
		return this.string;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ModVersionImpl that && this.string.equals(that.string);
	}

	@Override
	public int hashCode() {
		return this.string.hashCode();
	}

	public static ModVersion of(String string) {
		String trimmed = string.trim();

		if (trimmed.isBlank()) {
			throw new IllegalArgumentException("Cannot create a Version for an empty string");
		}

		List<VersionComponent> components = FlexVerComparator.decompose(trimmed);
		return new ModVersionImpl(trimmed, components);
	}
}
