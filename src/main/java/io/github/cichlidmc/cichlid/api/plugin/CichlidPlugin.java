package io.github.cichlidmc.cichlid.api.plugin;

import io.github.cichlidmc.cichlid.api.Cichlid;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Entrypoint for plugins.
 * Each method in this class will halt the loading process if it throws an exception.
 */
public interface CichlidPlugin {
	/**
	 * Called after all plugins have been loaded, right before the mod loading process begins.
	 * {@link Cichlid#plugins()} has been populated, and is now safe to query.
	 */
	default void init() {
	}

	/**
	 * Called after all mods have been loaded, right before the pre-launch entrypoint is invoked.
	 * {@link Cichlid#mods()} has been populated, and is now safe to query.
	 */
	default void afterModsLoaded() {
	}

	/**
	 * Configure the finder that will be used to find mod files.
	 */
	default void locateMods(ModFinder finder) {
	}

	/**
	 * Attempt to load the given path as a mod.
	 * This path may be a file or a directory. It could point to anywhere on the filesystem,
	 * as it may have been requested to be loaded by a different plugin.
	 * <p>
	 * If this plugin cannot load the given path, return null.
	 */
	@Nullable
	default LoadableMod loadMod(Path path) {
		return null;
	}
}
