package io.github.cichlidmc.cichlid.impl.loading;

import java.util.Locale;

public enum LoadableType {
	PLUGIN, MOD;

	public final String name;
	public final String nameUpper;

	LoadableType() {
		this.name = this.name().toLowerCase(Locale.ROOT);
		char firstCharUpper = Character.toUpperCase(this.name.charAt(0));
		this.nameUpper = firstCharUpper + this.name.substring(1);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
