package fish.cichlidmc.cichlid.api;

import fish.cichlidmc.cichlid.api.loaded.LoadedSet;
import fish.cichlidmc.cichlid.api.loaded.Mod;
import fish.cichlidmc.cichlid.api.loaded.Plugin;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.impl.CichlidImpl;

/// Central interface into Cichlid.
public final class Cichlid {
	/// The ID used for the implicit mod representing Cichlid.
	public static final String ID = "cichlid";

	private Cichlid() {}

	/// The currently running [ModVersion] of Cichlid.
	public static ModVersion version() {
		return CichlidImpl.VERSION.get();
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
