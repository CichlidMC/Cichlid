package fish.cichlidmc.cichlid.impl.loaded;

import fish.cichlidmc.cichlid.api.loaded.Plugin;
import fish.cichlidmc.cichlid.api.metadata.Metadata;

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
