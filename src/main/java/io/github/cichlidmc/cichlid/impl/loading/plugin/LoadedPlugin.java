package io.github.cichlidmc.cichlid.impl.loading.plugin;

import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.plugin.CichlidPlugin;

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
