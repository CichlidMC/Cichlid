package fish.cichlidmc.cichlid.impl;

import fish.cichlidmc.cichlid.api.Cichlid;
import fish.cichlidmc.cichlid.api.CichlidPaths;
import fish.cichlidmc.cichlid.api.dist.Distribution;
import fish.cichlidmc.cichlid.api.loaded.LoadedSet;
import fish.cichlidmc.cichlid.api.loaded.Mod;
import fish.cichlidmc.cichlid.api.loaded.Plugin;
import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.api.mod.entrypoint.EarlySetupEntrypoint;
import fish.cichlidmc.cichlid.api.mod.entrypoint.EntrypointHelper;
import fish.cichlidmc.cichlid.api.mod.entrypoint.PreLaunchEntrypoint;
import fish.cichlidmc.cichlid.api.version.Version;
import fish.cichlidmc.cichlid.impl.loading.mod.ModLoader;
import fish.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import fish.cichlidmc.cichlid.impl.loading.plugin.PluginLoader;
import fish.cichlidmc.cichlid.impl.logging.CichlidLogger;
import fish.cichlidmc.cichlid.impl.metadata.component.condition.ConditionRegistry;
import fish.cichlidmc.cichlid.impl.sushi.BuiltInSushiTransformers;
import fish.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import fish.cichlidmc.cichlid.impl.util.FileUtils;
import fish.cichlidmc.cichlid.impl.util.MinecraftEntrypoint;
import fish.cichlidmc.cichlid.impl.util.Utils;
import fish.cichlidmc.fishflakes.api.value.Late;
import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class CichlidImpl {
	// cichlid is initialized if any value has been set (the value will always be null)
	public static final Late.Mutable<@Nullable Void> INITIALIZED = Late.unset();

	public static final Late.Mutable<Version> VERSION = Late.unset();
	public static final Late.Mutable<Distribution> DISTRIBUTION = Late.unset();
	public static final Late.Mutable<Version> MINECRAFT_VERSION = Late.unset();
	public static final Late.Mutable<Instrumentation> INSTRUMENTATION = Late.unset();
	public static final Late.Mutable<LoadedSet<Plugin>> PLUGINS = Late.unset();
	public static final Late.Mutable<LoadedSet<Mod>> MODS = Late.unset();

	public static final String CICHLID_VERSION_FILE = "cichlid_version.txt";
	public static final String MINECRAFT_VERSION_FILE = "version.json";
	public static final String BRAND = "Cichlid";

	@Nullable
	public static final String DISTRIBUTION_OVERRIDE = System.getProperty(propertyName("distribution.override"));

	private static final CichlidLogger logger = CichlidLogger.get("Cichlid");
	private static final ClassLoader classLoader = CichlidImpl.class.getClassLoader();

	public static Id id(String path) {
		return new Id(Cichlid.ID, path);
	}

	public static String propertyName(String suffix) {
		return "fish.cichlidmc.cichlid." + suffix;
	}

	public static void load(@Nullable String agentArgs, Instrumentation instrumentation) {
		if (INITIALIZED.isSet()) {
			throw new IllegalStateException("Cichlid is already loaded!");
		}

		logger.info("Cichlid initializing!");

		if (agentArgs != null) {
			logger.warn("Ignoring agent args: " + agentArgs);
		}

		Path resources = findResourcesRoot();
		Path versionFile = resources.resolve(CICHLID_VERSION_FILE);

		try (BufferedReader reader = Files.newBufferedReader(versionFile)) {
			String content = reader.readAllAsString().trim();
			VERSION.set(Version.of(content));
		} catch (IOException e) {
			throw new RuntimeException("Failed to read Cichlid version", e);
		}

		logger.info("Version: " + Cichlid.version());
		logger.space();

		DISTRIBUTION.set(detectDistribution());

		try {
			MINECRAFT_VERSION.set(detectMinecraftVersion());
		} catch (IOException e) {
			throw new RuntimeException("Failed to detect Minecraft version", e);
		}

		logger.info("Loading Minecraft " + Cichlid.minecraftVersion() + " (" + Cichlid.distribution() + ')');
		logger.space();

		INSTRUMENTATION.set(instrumentation);

		logger.info("Bootstrapping...");
		Sushi.bootstrap();
		ConditionRegistry.bootstrap();
		CichlidTransformer.init(instrumentation);

		logger.info("Loading plugins...");
		Map<String, LoadedPlugin> loadedPlugins = PluginLoader.load(instrumentation);
		PLUGINS.set(PluginLoader.toLoadedSet(loadedPlugins));
		logLoadedSet(Cichlid.plugins(), "plugin", Plugin::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.init());

		logger.space();

		logger.info("Loading mods...");
		MODS.set(ModLoader.load(loadedPlugins, instrumentation));
		logLoadedSet(Cichlid.mods(), "mod", Mod::metadata);

		loadedPlugins.values().forEach(plugin -> plugin.impl.afterModsLoaded());

		EntrypointHelper.invoke(EarlySetupEntrypoint.class, EarlySetupEntrypoint.KEY, EarlySetupEntrypoint::earlySetup, true);

		CichlidTransformer.initSushi(builder -> {
			try {
				loadSushiTransformers(builder, resources);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load Sushi transformers", e);
			}
		});

		INITIALIZED.set(null);
		logger.info("Cichlid initialized!");

		logger.space();

		EntrypointHelper.invoke(PreLaunchEntrypoint.class, PreLaunchEntrypoint.KEY, PreLaunchEntrypoint::preLaunch, true);

		logger.info("Continuing to Minecraft...");

		logger.space();
	}

	private static void loadSushiTransformers(TransformerManager.Builder builder, Path cichlidResources) throws IOException {
		BuiltInSushiTransformers.register(builder, cichlidResources);

		Path output = CichlidPaths.CICHLID_ROOT.resolve(".sushi").resolve("output");
		FileUtils.deleteRecursively(output);
		Files.createDirectories(output);

		for (Mod mod : Cichlid.mods()) {
			if (mod.resources().isEmpty())
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
					// builder.parseAndRegister(id, json).ifPresent(error -> {
					// 	throw new RuntimeException("Failed to register Sushi transformer " + id + ": " + error);
					// });
				} catch (Id.InvalidException e) {
					throw new RuntimeException("Sushi transformer in mod " + mod.metadata().blame() + " has an invalid name", e);
				}
			});
		}
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

	private static Path findResourcesRoot() {
		URL versionFileUrl = classLoader.getResource(CICHLID_VERSION_FILE);
		if (versionFileUrl == null) {
			throw new IllegalStateException("Version file URL was not found");
		}

		URI uri = Utils.toUri(versionFileUrl);

		try {
			// if we're running from a jar, try to open the filesystem
			//noinspection resource - we want it to stay open
			FileSystems.newFileSystem(uri, Map.of());
		} catch (IOException _) {}

		try {
			Path path = Paths.get(versionFileUrl.toURI());
			return Objects.requireNonNull(path.getParent(), "parent");
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Version file URL is not a valid Path", e);
		}
	}

	private static Distribution detectDistribution() {
		if (DISTRIBUTION_OVERRIDE != null) {
			Distribution distribution = Distribution.of(DISTRIBUTION_OVERRIDE);
			if (distribution == null) {
				throw new IllegalStateException("Invalid distribution override: " + DISTRIBUTION);
			}

			logger.info("Detected distribution has been overridden to " + distribution);
			return distribution;
		}

		Set<MinecraftEntrypoint> foundEntrypoints = EnumSet.noneOf(MinecraftEntrypoint.class);

		for (MinecraftEntrypoint entrypoint : MinecraftEntrypoint.values()) {
			String resourcePath = entrypoint.className.replace('.', '/') + ".class";
			if (classLoader.getResource(resourcePath) != null) {
				foundEntrypoints.add(entrypoint);
			}
		}

		if (foundEntrypoints.size() == 1) {
			return foundEntrypoints.iterator().next().distribution;
		}

		throw new IllegalStateException("The current distribution of Minecraft could not be automatically determined");
	}

	private static Version detectMinecraftVersion() throws IOException {
		InputStream stream = classLoader.getResourceAsStream(MINECRAFT_VERSION_FILE);
		if (stream == null) {
			throw new IOException("Minecraft version file is missing");
		}

		try (InputStreamReader reader = new InputStreamReader(stream)) {
			JsonString string = TinyJson.parse(reader).asObject().getOrThrow("id").asString();
			return Version.of(string.value());
		} catch (JsonException e) {
			throw new IOException("Failed to parse Minecraft version file", e);
		}
	}
}
