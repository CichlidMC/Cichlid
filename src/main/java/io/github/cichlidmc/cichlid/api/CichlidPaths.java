package io.github.cichlidmc.cichlid.api;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Common paths used by Cichlid, mods, and plugins.
 */
public class CichlidPaths {
	/**
	 * Minecraft's root directory, usually ".minecraft"
	 */
	public static final Path MINECRAFT_ROOT = Paths.get("");
	/**
	 * Root for Cichlid files, ".minecraft/cichlid"
	 */
	public static final Path CICHLID_ROOT = MINECRAFT_ROOT.resolve("cichlid");
	/**
	 * Standard mods folder, ".minecraft/cichlid/mods"
	 */
	public static final Path MODS = CICHLID_ROOT.resolve("mods");
	/**
	 * Plugins folder, ".minecraft/cichlid/plugins"
	 */
	public static final Path PLUGINS = CICHLID_ROOT.resolve("plugins");
	/**
	 * Configs folder, ".minecraft/cichlid/configs"
	 */
	public static final Path CONFIGS = CICHLID_ROOT.resolve("configs");
	/**
	 * Files cached by Cichlid, ".minecraft/cichlid/.cache"
	 */
	public static final Path CACHE = CICHLID_ROOT.resolve(".cache");
}
