package fish.cichlidmc.cichlid.api.mod.entrypoint;

import fish.cichlidmc.cichlid.api.loaded.Mod;

/// Invoked during Cichlid initialization, before Minecraft has started.
///
/// At this point, Sushi has been fully initialized, so it should be safe to access any class, ignoring side effects.
///
/// Implementations are identified by the `pre_launch` key. They should implement this interface and have a public, no-arg constructor.
/// @see EarlySetupEntrypoint
public interface PreLaunchEntrypoint {
	String KEY = "pre_launch";

	/// @param mod this mod
	void preLaunch(Mod mod);
}
