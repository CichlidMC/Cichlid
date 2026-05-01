package fish.cichlidmc.cichlid.impl.loading.plugin.builtin;

import fish.cichlidmc.cichlid.api.Cichlid;
import fish.cichlidmc.cichlid.api.metadata.ModMetadata;
import fish.cichlidmc.cichlid.api.plugin.CichlidPlugin;
import fish.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import fish.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import fish.cichlidmc.cichlid.api.plugin.mod.LoadedMod;
import fish.cichlidmc.cichlid.impl.loading.plugin.LoadablePlugin;
import fish.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import fish.cichlidmc.cichlid.impl.metadata.PluginMetadata;
import fish.cichlidmc.cichlid.impl.util.FileUtils;
import fish.cichlidmc.cichlid.impl.util.Utils;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public enum StandardJarPlugin implements CichlidPlugin {
	INSTANCE;

	public static final String EXTENSION = ".cld";
	public static final String MOD_METADATA = "cichlid.mod.json";
	public static final String ID = "standard";

	public static final PluginMetadata METADATA = new PluginMetadata(
			ID, "Cichlid Standard Plugin", Cichlid.version(),
			"Built-in plugin for loading standard " + EXTENSION + " Cichlid mods",
			Utils.mapOf("CichlidMC", "Author"),
			Map.of(), Map.of(), Map.of(),
			StandardJarPlugin.class.getName()
	);

	public static final LoadablePlugin.BuiltIn LOADABLE = new LoadablePlugin.BuiltIn(METADATA, INSTANCE);
	public static final LoadedPlugin LOADED = LOADABLE.loaded;

	@Override
	@Nullable
	public LoadableMod loadMod(Path path) {
		if (!FileUtils.nameEndsWith(path, EXTENSION))
			return null;

		// TODO: JiJ
		File file = FileUtils.toFileOrNull(path);
		if (file == null)
			return null;

		try (JarFile jar = new JarFile(file)) {
			JarEntry metadataEntry = jar.getJarEntry(MOD_METADATA);
			if (metadataEntry == null)
				return null;

			InputStream stream = jar.getInputStream(metadataEntry);
			JsonValue json = TinyJson.parse(stream);
			ModMetadata metadata = ModMetadataBuilder.fromJson(json.asObject());
			return new LoadableJarMod(metadata, path);
		} catch (JsonException e) {
			throw new RuntimeException("Mod contains malformed metadata: " + path, e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final class LoadableJarMod extends LoadableMod {
		private final Path file;

		private LoadableJarMod(ModMetadata metadata, Path file) {
			super(metadata, file.toString());
			this.file = file;
		}

		@Override
		public LoadedMod load() throws IOException {
			JarFile jar = new JarFile(this.file.toFile());
			return LoadedMod.create(jar);
		}
	}
}
