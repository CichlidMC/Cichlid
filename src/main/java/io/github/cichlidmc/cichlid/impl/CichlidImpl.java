package io.github.cichlidmc.cichlid.impl;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.api.dist.Distribution;
import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;
import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.EarlySetupEntrypoint;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.EntrypointHelper;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.PreLaunchEntrypoint;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.loading.mod.ModLoader;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import io.github.cichlidmc.cichlid.impl.loading.plugin.PluginLoader;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import io.github.cichlidmc.cichlid.impl.transformer.remap.MinecraftRemapper;
import io.github.cichlidmc.cichlid.impl.util.FileUtils;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
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

	public static void load(@Nullable String stringArgs, Instrumentation instrumentation) {
		if (isInitialized()) {
			throw new IllegalStateException("Cichlid is already loaded!");
		}

		logger.info("Cichlid initializing!");
		readVersion();
		logger.info("Cichlid version: " + version());

		logger.space();

		logger.info("Parsing arguments...");

		CichlidArgs args = parseArguments(stringArgs);
		mcVersion = Version.of(args.version);
		dist = args.dist;

		logger.info("Minecraft version: " + mcVersion());
		logger.info("Distribution: " + distribution());

		logger.space();

		// register transformer now so it can catch early classloading
		CichlidTransformer transformer = CichlidTransformer.setup(instrumentation);

		try {
			// make sure this class is loaded early
			Class.forName("net.neoforged.art.internal.EnhancedRemapper", false, CichlidImpl.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		transformer.setRemapper(getRemapper(args.reverseMappings));

		logger.info("Loading plugins...");
		Map<String, LoadedPlugin> loadedPlugins = PluginLoader.load(instrumentation);
		plugins = PluginLoader.toLoadedSet(loadedPlugins);
		logLoadedSet(plugins(), "plugin", Plugin::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.init());

		logger.space();

		logger.info("Loading mods...");
		mods = ModLoader.load(loadedPlugins, instrumentation);
		logLoadedSet(mods(), "mod", Mod::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.afterModsLoaded());

		EntrypointHelper.invoke(EarlySetupEntrypoint.class, EarlySetupEntrypoint.KEY, EarlySetupEntrypoint::earlySetup, true);

		try {
			transformer.setTransformerManager(loadSushiTransformers());
		} catch (IOException e) {
			throw new RuntimeException("Failed to setup Sushi", e);
		}

		initialized = true;
		logger.info("Cichlid initialized!");

		logger.space();

		EntrypointHelper.invoke(PreLaunchEntrypoint.class, PreLaunchEntrypoint.KEY, PreLaunchEntrypoint::preLaunch, true);

		logger.info("Continuing to Minecraft...");
		logger.space();
	}

	@Nullable
	private static MinecraftRemapper getRemapper(boolean reverse) {
		Path file = CichlidPaths.CICHLID_ROOT.resolve(".meta").resolve("mappings.txt");
		if (!Files.exists(file)) {
			logger.info("No mappings found, proceeding without them");
			return null;
		}

		try {
			logger.info("Loading mappings...");
			long start = System.currentTimeMillis();
			IMappingFile mappings = IMappingFile.load(file.toFile());
			long seconds = (System.currentTimeMillis() - start) / 1000;
			logger.info("Mappings successfully loaded in " + seconds + " second(s)");
			return new MinecraftRemapper(reverse ? mappings.reverse() : mappings);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read mappings", e);
		}
	}

	private static TransformerManager loadSushiTransformers() throws IOException {
		Path output = CichlidPaths.CICHLID_ROOT.resolve(".sushi").resolve("output");
		FileUtils.deleteRecursively(output);
		Files.createDirectories(output);

		TransformerManager.Builder builder = TransformerManager.builder();
		builder.output(output);

		for (Mod mod : mods()) {
			if (!mod.resources().isPresent())
				continue;

			Path transformers = mod.resources().get().resolve("transformers");
			if (!Files.exists(transformers))
				continue;

			FileUtils.walkFiles(transformers, file -> {
				String path = transformers.relativize(file).toString();
				if (!path.endsWith(".sushi"))
					return;

				String withoutExtension = path.substring(0, path.length() - ".sushi".length());
				try {
					Id id = new Id(mod.metadata().id(), withoutExtension);
					JsonValue json = TinyJson.parse(file);
					builder.parseAndRegister(id, json).ifPresent(error -> {
						throw new RuntimeException("Failed to register Sushi transformer " + id + ": " + error);
					});
				} catch (Id.InvalidException e) {
					throw new RuntimeException("Sushi transformer in mod " + mod.metadata().blame() + " has an invalid name", e);
				}
			});
		}

		return builder.build();
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

	private static CichlidArgs parseArguments(@Nullable String args) {
		CichlidArgs parsed = CichlidArgs.parse(args);
		if (parsed == null) {
			throw new RuntimeException("Cichlid is not installed properly! Invalid arguments: " + args);
		}

		return parsed;
	}
}
