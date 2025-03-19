package io.github.cichlidmc.cichlid.impl.version.parser.token;

import io.github.cichlidmc.cichlid.api.version.Version;

public class VersionToken implements Token {
	public final Version version;

	public VersionToken(Version version) {
		this.version = version;
	}

	public VersionToken(String version) {
		this(Version.of(version));
	}

	@Override
	public int length() {
		return this.version.toString().length();
	}

	@Override
	public String toString() {
		return this.version.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof VersionToken && ((VersionToken) obj).version.equals(this.version);
	}
}
