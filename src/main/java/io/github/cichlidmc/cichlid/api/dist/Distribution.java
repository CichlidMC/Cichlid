package io.github.cichlidmc.cichlid.api.dist;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum Distribution {
	CLIENT, DEDICATED_SERVER;

	/**
	 * The snake_case name of this distribution.
	 */
	public final String name = this.name().toLowerCase(Locale.ROOT);

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Try to parse a Distribution from the given snake_case name, returning null if invalid.
	 */
	@Nullable
	public static Distribution of(String name) {
		if ("client".equals(name)) {
			return CLIENT;
		} else if ("dedicated_server".equals(name)) {
			return DEDICATED_SERVER;
		} else {
			return null;
		}
	}
}
