package fish.cichlidmc.cichlid.impl.version;

import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.impl.version.FlexVerComparator.VersionComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record VersionImpl(String string, List<VersionComponent> components) implements Version {
	@Override
	public int compareTo(@NotNull Version o) {
		return FlexVerComparator.compare(this.components, ((VersionImpl) o).components);
	}

	@Override
	public String toString() {
		return this.string;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VersionImpl that && this.string.equals(that.string);
	}

	@Override
	public int hashCode() {
		return this.string.hashCode();
	}

	public static Version of(String string) {
		List<VersionComponent> components = FlexVerComparator.decompose(string);
		return new VersionImpl(string, components);
	}
}
