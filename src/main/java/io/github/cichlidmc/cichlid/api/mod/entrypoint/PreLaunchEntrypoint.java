package io.github.cichlidmc.cichlid.api.mod.entrypoint;

import io.github.cichlidmc.cichlid.api.loaded.Mod;

/**
 * Invoked during Cichlid initialization, before the primary application has started.
 * <br>
 * Be careful with what classes you access. Some may not be available yet, and some may cause problems when loaded early.
 * <br>
 * Implementations are identified by the "pre_launch" key. They should implement this interface
 * and have a public, no-arg constructor.
 */
public interface PreLaunchEntrypoint {
	String KEY = "pre_launch";

	/**
	 * @param mod this mod
	 */
	void preLaunch(Mod mod);
}
