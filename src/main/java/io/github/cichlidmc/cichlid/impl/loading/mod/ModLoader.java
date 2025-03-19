package io.github.cichlidmc.cichlid.impl.loading.mod;

import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;
import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadedMod;
import io.github.cichlidmc.cichlid.impl.loaded.ModImpl;
import io.github.cichlidmc.cichlid.impl.loading.ClasspathLoadableFinder;
import io.github.cichlidmc.cichlid.impl.loading.DependencyChecker;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import io.github.cichlidmc.cichlid.impl.loading.plugin.builtin.StandardJarPlugin;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;
import io.github.cichlidmc.cichlid.impl.util.LoadedSetImpl;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModLoader {
	public static LoadedSet<Mod> load(Map<String, LoadedPlugin> plugins, Instrumentation instrumentation) {
		try {
			Map<String, Mod> map = new HashMap<>();
			doLoad(plugins, map, instrumentation);
			return new LoadedSetImpl<>(map);
		} catch (IOException e) {
			throw new RuntimeException("Exception occurred while loading mods", e);
		}
	}

	private static void doLoad(Map<String, LoadedPlugin> plugins, Map<String, Mod> map, Instrumentation instrumentation) throws IOException {
		Map<String, LoadableMod> loadableMods = new HashMap<>();
		ProblemReport report = new ProblemReport();
		ClasspathLoadableFinder.forMods().find(loadableMods, report);

		Map<LoadableMod, LoadedPlugin> loaders = new IdentityHashMap<>();
		// all classpath-loaded mods are assumed standard
		loadableMods.values().forEach(loadable -> loaders.put(loadable, StandardJarPlugin.LOADED));

		for (Path mod : findModsFromPlugins(plugins.values())) {
			Map<LoadedPlugin, LoadableMod> provided = new IdentityHashMap<>();
			for (LoadedPlugin plugin : plugins.values()) {
				try {
					LoadableMod loadable = plugin.impl.loadMod(mod);
					if (loadable != null) {
						provided.put(plugin, loadable);
					}
				} catch (Throwable t) {
					// this is fatal, throw now
					throw new RuntimeException("Exception thrown by plugin while loading mod at " + mod, t);
				}
			}

			if (provided.isEmpty()) {
				report.addSection("Mod file was not recognized by any plugins", mod);
			} else if (provided.size() == 1) {
				Map.Entry<LoadedPlugin, LoadableMod> entry = provided.entrySet().iterator().next();
				LoadableMod loadable = entry.getValue();
				if (!handleDuplicate(loadableMods, loadable.metadata, report, mod.toString())) {
					loadableMods.put(loadable.metadata.id(), loadable);
					loaders.put(loadable, entry.getKey());
				}
			} else {
				// more than one plugin recognized this file. this is a conflict.
				List<ReportDetail> details = new ArrayList<>();
				details.add(new ReportDetail("Location", mod.toString()));

				for (LoadedPlugin plugin : provided.keySet()) {
					details.add(new ReportDetail("Recognized by Plugin", plugin.representation.metadata().blame()));
				}

				report.addSection("Mod file was recognized by multiple plugins. Only one plugin may load each mod.", details);
			}
		}

		// check if any mods have an ID conflicting with a plugin
		for (LoadableMod mod : loadableMods.values()) {
			ModMetadata modMetadata = mod.metadata;
			LoadedPlugin plugin = plugins.get(modMetadata.id());
			if (plugin == null)
				continue;

			Metadata pluginMetadata = plugin.representation.metadata();

			report.addSection(
					"A mod and a plugin are conflicting, and have the same ID",
					new ReportDetail("ID", modMetadata.id()),
					new ReportDetail("Mod Name", modMetadata.name()),
					new ReportDetail("Mod Source", mod.source),
					new ReportDetail("Plugin Name", pluginMetadata.name()),
					new ReportDetail("Plugin Source", plugin.source)
			);
		}

		report.throwIfNotEmpty();
		DependencyChecker.forMods(plugins, loadableMods).check(report);
		report.throwIfNotEmpty();

		// do the loading
		loadableMods.forEach((id, loadable) -> {
			try {
				LoadedMod loaded = loadable.load();
				LoadedPlugin plugin = Objects.requireNonNull(loaders.get(loadable), () -> "LoadableMod is missing its plugin: " + id);

				loaded.jar().ifPresent(instrumentation::appendToSystemClassLoaderSearch);
				Mod mod = new ModImpl(loadable.metadata, plugin.representation, loaded.resources());
				map.put(id, mod);
			} catch (Throwable t) {
				throw new RuntimeException("Error while loading mod " + loadable.metadata.blame(), t);
			}
		});
	}

	private static Set<Path> findModsFromPlugins(Collection<LoadedPlugin> plugins) throws IOException {
		ModFinderImpl finder = new ModFinderImpl();
		for (LoadedPlugin plugin : plugins) {
			try {
				plugin.impl.locateMods(finder);
			} catch (Throwable t) {
				throw new RuntimeException("Exception thrown by plugin while finding mods", t);
			}
		}

		return finder.find();
	}

	private static boolean handleDuplicate(Map<String, LoadableMod> map, Metadata metadata, ProblemReport report, String location) {
		LoadableMod existing = map.get(metadata.id());
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

		report.addSection("Duplicate mod", details);
		return true;
	}
}
