package io.github.cichlidmc.cichlid.impl.version;

import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.version.FlexVerComparator.VersionComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionImpl implements Version {
	private final String string;
	private final List<VersionComponent> components;

	private VersionImpl(String string, List<VersionComponent> components) {
		this.string = string;
		this.components = components;
	}

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
		return obj instanceof VersionImpl && ((VersionImpl) obj).string.equals(this.string);
	}

	public static Version of(String string) {
		List<VersionComponent> components = FlexVerComparator.decompose(string);
		return new VersionImpl(string, components);
	}
}
