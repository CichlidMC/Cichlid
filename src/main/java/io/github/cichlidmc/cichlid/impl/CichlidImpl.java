package io.github.cichlidmc.cichlid.impl;

import io.github.cichlidmc.cichlid.api.dist.Distribution;
import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;
import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.EntrypointHelper;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.PreLaunchEntrypoint;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.loading.mod.ModLoader;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import io.github.cichlidmc.cichlid.impl.loading.plugin.PluginLoader;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformerManager;
import io.github.cichlidmc.cichlid.impl.transformer.EnvironmentStripper;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.function.Function;

public class CichlidImpl {
	private static final CichlidLogger logger = CichlidLogger.get("Cichlid");

	private static boolean initialized;
	private static Version version;
	private static Distribution dist;
	private static Version mcVersion;
	private static LoadedSet<Plugin> plugins;
	private static LoadedSet<Mod> mods;

	public static boolean isInitialized() {
		return initialized;
	}

	public static Version version() {
		return Utils.getOrThrow(version, "Cichlid version is not loaded yet");
	}

	public static Version mcVersion() {
		return Utils.getOrThrow(mcVersion, "Minecraft version is not loaded yet");
	}

	public static Distribution distribution() {
		return Utils.getOrThrow(dist, "Distribution is not loaded yet");
	}

	public static LoadedSet<Plugin> plugins() {
		return Utils.getOrThrow(plugins, "Plugins are not loaded yet");
	}

	public static LoadedSet<Mod> mods() {
		return Utils.getOrThrow(mods, "Mods are not loaded yet");
	}

	public static void load(@Nullable String args, Instrumentation instrumentation) {
		if (initialized) {
			throw new IllegalStateException("Cichlid is already loaded!");
		}

		logger.info("Cichlid initializing!");
		readVersion();
		logger.info("Cichlid version: " + version());

		logger.space();

		logger.info("Parsing arguments...");
		parseArguments(args);
		logger.info("Minecraft version: " + mcVersion());
		logger.info("Distribution: " + distribution());

		logger.space();

		// register transformer now so it can catch early classloading
		instrumentation.addTransformer(CichlidTransformerManager.INSTANCE);
		CichlidTransformerManager.registerTransformer(EnvironmentStripper.INSTANCE);

		logger.info("Loading plugins...");
		Map<String, LoadedPlugin> loadedPlugins = PluginLoader.load(instrumentation);
		plugins = PluginLoader.toLoadedSet(loadedPlugins);
		logLoadedSet(plugins(), "plugin", Plugin::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.beforeModsLoad());

		logger.space();

		logger.info("Loading mods...");
		mods = ModLoader.load(loadedPlugins, instrumentation);
		logLoadedSet(mods(), "mod", Mod::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.afterModsLoaded());

		initialized = true;
		logger.info("Cichlid initialized!");

		logger.space();

		logger.info("Invoking pre-launch...");
		EntrypointHelper.invoke(PreLaunchEntrypoint.class, PreLaunchEntrypoint.KEY, PreLaunchEntrypoint::preLaunch);
		logger.info("Pre-launch done. Continuing to Minecraft...");

		logger.space();
	}

	private static <T> void logLoadedSet(LoadedSet<T> set, String type, Function<T, Metadata> metadata) {
		if (set.isEmpty()) {
			logger.info("Loading 0 " + type + "s.");
		} else if (set.size() == 1) {
			T value = set.iterator().next();
			String id = metadata.apply(value).id();
			logger.info("Loading 1 " + type + ": " + id);
		} else {
			logger.info("Loading " + set.size() + ' ' + type + "s:");
			for (T value : set) {
				String id = metadata.apply(value).id();
				logger.info("\t- " + id);
			}
		}
	}

	private static void readVersion() {
		// read version
		InputStream stream = CichlidImpl.class.getClassLoader().getResourceAsStream("cichlid_version.txt");
		if (stream == null) {
			throw new RuntimeException("Cichlid version is missing");
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String string = reader.readLine().trim();
			if (string.equals("${version}")) {
				throw new RuntimeException("Cichlid version is not set");
			}
			version = Version.of(string);
		} catch (IOException e) {
			throw new RuntimeException("Error reading Cichlid version", e);
		}
	}

	private static void parseArguments(@Nullable String args) {
		CichlidArgs parsed = CichlidArgs.parse(args);
		if (parsed == null) {
			throw new RuntimeException("Cichlid is not installed properly! Invalid arguments: " + args);
		}

		mcVersion = Version.of(parsed.version);
		dist = parsed.dist;
	}
}
