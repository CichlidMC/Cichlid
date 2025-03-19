package io.github.cichlidmc.cichlid.impl.loaded;

import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.metadata.Metadata;

public final class PluginImpl implements Plugin {
	private final Metadata metadata;

	public PluginImpl(Metadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public Metadata metadata() {
		return this.metadata;
	}
}
