package fish.cichlidmc.cichlid.api.mod.entrypoint;

import fish.cichlidmc.cichlid.api.loaded.Mod;

/// Invoked during Cichlid initialization, before Minecraft has started, and before Sushi has initialized.
///
/// This entrypoint should only be used when strictly necessary. For a more reasonable pre-launch entrypoint, see [PreLaunchEntrypoint].
/// Mods may use this entrypoint for things that need to be done extremely early, like registering new Sushi transform types.
///
/// Since Sushi is not yet initialized, bytecode transformation cannot occur yet. This means Minecraft classes are off-limits,
/// and attempting to load them will fail. (specifically, classes under `net.minecraft` and `com.mojang`.)
///
/// Be aware that all classes you load will not be transformable by other mods. Try to minimize the damages.
///
/// Implementations are identified by the `early_setup` key. They should implement this interface and have a public, no-arg constructor.
/// @see PreLaunchEntrypoint
public interface EarlySetupEntrypoint {
	String KEY = "early_setup";

	/// @param mod this mod
	void earlySetup(Mod mod);
}
