package io.github.cichlidmc.cichlid.impl.loading.plugin;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;
import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.impl.loading.ClasspathLoadableFinder;
import io.github.cichlidmc.cichlid.impl.loading.DependencyChecker;
import io.github.cichlidmc.cichlid.impl.loading.SharedLoading;
import io.github.cichlidmc.cichlid.impl.loading.plugin.builtin.StandardJarPlugin;
import io.github.cichlidmc.cichlid.impl.metadata.PluginMetadata;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;
import io.github.cichlidmc.cichlid.impl.util.Either;
import io.github.cichlidmc.cichlid.impl.util.FileUtils;
import io.github.cichlidmc.cichlid.impl.util.LoadedSetImpl;
import io.github.cichlidmc.tinyjson.JsonException;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public class PluginLoader {
	public static final String EXTENSION = ".clp";
	public static final String METADATA = "cichlid.plugin.json";

	public static LoadedSet<Plugin> toLoadedSet(Map<String, LoadedPlugin> map) {
		Map<String, Plugin> plugins = new HashMap<>();
		map.forEach((id, loaded) -> plugins.put(id, loaded.representation));
		return new LoadedSetImpl<>(plugins);
	}

	public static Map<String, LoadedPlugin> load(Instrumentation instrumentation) {
		try {
			return doLoad(instrumentation);
		} catch (IOException e) {
			throw new RuntimeException("IOException while loading plugins", e);
		}
	}

	private static Map<String, LoadedPlugin> doLoad(Instrumentation instrumentation) throws IOException {
		// find plugin files recursively
		Set<Path> files = new HashSet<>();
		FileUtils.walkFiles(CichlidPaths.PLUGINS, files::add);

		Map<String, LoadablePlugin> loadable = new HashMap<>();
		addBuiltInPlugins(loadable);
		ProblemReport report = new ProblemReport();
		ClasspathLoadableFinder.forPlugins().find(loadable, report);

		// go through each file and try to create a LoadablePlugin from it
		for (Path file : files) {
			if (FileUtils.isDisabled(file))
				continue;

			if (!FileUtils.nameEndsWith(file, EXTENSION)) {
				report.addSection("File is not a Cichlid plugin", file);
				continue;
			}

			try (FileSystem fs = FileUtils.openJar(file)) {
				Path metadataFile = fs.getPath(METADATA);
				if (!Files.exists(metadataFile)) {
					report.addSection("File does not contain Cichlid plugin metadata", file);
					continue;
				}

				Either<PluginMetadata, JsonException> result = SharedLoading.parseMetadata(metadataFile.toUri(), PluginMetadata::fromJson);
				if (result.isRight()) {
					report.addSection(
							"Plugin has malformed metadata",
							new ReportDetail("Location", file.toString()),
							new ReportDetail("Error", result.right().getMessage())
					);

					continue;
				}

				PluginMetadata metadata = result.left();
				if (handleDuplicate(loadable, metadata, report, file.toString()))
					continue;

				JarFile jar = new JarFile(file.toFile());
				loadable.put(metadata.id(), new LoadablePlugin.Jar(metadata, jar));
			}
		}

		// don't handle dependencies if any errors have already occurred, will likely just create nonsense
		report.throwIfNotEmpty();
		DependencyChecker.forPlugins(loadable).check(report);
		report.throwIfNotEmpty();

		// load the plugins
		Map<String, LoadedPlugin> loaded = new HashMap<>();
		loadable.forEach((id, plugin) -> plugin.load(instrumentation, report).ifPresent(loadedPlugin -> loaded.put(id, loadedPlugin)));

		report.throwIfNotEmpty();

		return loaded;
	}

	private static void addBuiltInPlugins(Map<String, LoadablePlugin> loadable) {
		loadable.put(StandardJarPlugin.ID, StandardJarPlugin.LOADABLE);
	}

	private static boolean handleDuplicate(Map<String, LoadablePlugin> map, Metadata metadata, ProblemReport report, String location) {
		LoadablePlugin existing = map.get(metadata.id());
		if (existing == null)
			return false;

		List<ReportDetail> details = new ArrayList<>();
		details.add(new ReportDetail("Name", metadata.name()));
		details.add(new ReportDetail("ID", metadata.id()));
		details.add(new ReportDetail("Location", location));

		if (!metadata.name().equals(existing.metadata.name())) {
			details.add(new ReportDetail("Other Name", existing.metadata.name()));
		}

		details.add(new ReportDetail("Other Location", existing.source));

		report.addSection("Duplicate plugin", details);
		return true;
	}
}
