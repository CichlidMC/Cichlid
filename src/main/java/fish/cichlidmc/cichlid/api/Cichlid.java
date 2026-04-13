package fish.cichlidmc.cichlid.api;

import fish.cichlidmc.cichlid.api.dist.Distribution;
import fish.cichlidmc.cichlid.api.loaded.LoadedSet;
import fish.cichlidmc.cichlid.api.loaded.Mod;
import fish.cichlidmc.cichlid.api.loaded.Plugin;
import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.impl.CichlidImpl;

/// Core Cichlid APIs for loader, game, plugin, and mod information.
public final class Cichlid {
	/// The ID used for the implicit mod representing Cichlid.
	public static final String ID = "cichlid";

	private Cichlid() {}

	/// The currently running [Version] of Cichlid.
	public static Version version() {
		return CichlidImpl.VERSION.get();
	}

	/// The currently loaded [Version] of Minecraft.
	public static Version minecraftVersion() {
		return CichlidImpl.MINECRAFT_VERSION.get();
	}

	/// The current [Distribution] of Minecraft.
	public static Distribution distribution() {
		return CichlidImpl.DISTRIBUTION.get();
	}

	/// The set of currently loaded plugins.
	/// This method may not be called before plugins are loaded. Doing so will throw an exception.
	public static LoadedSet<Plugin> plugins() {
		return CichlidImpl.PLUGINS.get();
	}

	/// The set of currently loaded mods.
	/// This method may not be called before mods are loaded. Doing so will throw an exception.
	public static LoadedSet<Mod> mods() {
		return CichlidImpl.MODS.get();
	}
}
