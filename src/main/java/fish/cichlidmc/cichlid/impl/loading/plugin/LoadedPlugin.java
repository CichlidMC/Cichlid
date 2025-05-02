package fish.cichlidmc.cichlid.impl.loading.plugin;

import fish.cichlidmc.cichlid.api.loaded.Plugin;
import fish.cichlidmc.cichlid.api.plugin.CichlidPlugin;

public class LoadedPlugin {
	public final CichlidPlugin impl;
	public final Plugin representation;
	public final String source;

	public LoadedPlugin(CichlidPlugin impl, Plugin representation, String source) {
		this.impl = impl;
		this.representation = representation;
		this.source = source;
	}
}
