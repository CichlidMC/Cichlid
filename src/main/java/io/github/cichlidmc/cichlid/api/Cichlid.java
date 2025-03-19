package io.github.cichlidmc.cichlid.api;

import io.github.cichlidmc.cichlid.api.dist.Distribution;
import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;
import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.CichlidImpl;

/**
 * Core Cichlid APIs for loader, game, plugin, and mod information.
 */
public class Cichlid {
	/**
	 * The currently loaded Version of Cichlid.
	 */
	public static Version version() {
		return CichlidImpl.version();
	}

	/**
	 * The currently loaded Version of Minecraft.
	 */
	public static Version mcVersion() {
		return CichlidImpl.mcVersion();
	}

	/**
	 * The current Distribution of Minecraft.
	 */
	public static Distribution distribution() {
		return CichlidImpl.distribution();
	}

	/**
	 * The set of currently loaded plugins.
	 * This method may not be called before plugins are loaded. Doing so will throw an exception.
	 */
	public static LoadedSet<Plugin> plugins() {
		return CichlidImpl.plugins();
	}

	/**
	 * The set of currently loaded mods.
	 * This method may not be called before mods are loaded. Doing so will throw an exception.
	 */
	public static LoadedSet<Mod> mods() {
		return CichlidImpl.mods();
	}
}
