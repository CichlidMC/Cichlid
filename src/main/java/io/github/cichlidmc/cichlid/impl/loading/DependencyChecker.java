package io.github.cichlidmc.cichlid.impl.loading;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadablePlugin;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadedPlugin;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DependencyChecker {
	private final Map<String, TypedMetadata> metadata;
	private final LoadableType type;
	private final ConditionContext context;

	public DependencyChecker(Map<String, TypedMetadata> metadata, LoadableType type) {
		this.metadata = metadata;
		this.type = type;
		this.context = new ConditionContext();
	}

	public void check(ProblemReport report) {
		for (TypedMetadata metadata : this.metadata.values()) {
			if (metadata.type != this.type)
				continue;

			// check that dependencies are present
			this.checkDependencies(metadata.metadata, report);
			// and incompatibilities are not
			this.checkIncompatibilities(metadata.metadata, report);
		}
	}

	private void checkDependencies(Metadata metadata, ProblemReport report) {
		dependencies: for (Dependency dependency : metadata.dependencies().values()) {
			for (Condition condition : dependency.conditions()) {
				if (!condition.matches(this.context)) {
					continue dependencies;
				}
			}

			String depId = dependency.id();
			VersionPredicate predicate = dependency.predicate();
			Metadata required = this.get(depId);

			if (required == null) {
				List<ReportDetail> details = Utils.mutableListOf(
						new ReportDetail(this.type.nameUpper, metadata.blame()),
						new ReportDetail("ID of Dependency", depId),
						new ReportDetail("Allowed Versions", predicate.toString())
				);

				String source = dependency.source();
				if (source != null) {
					details.add(new ReportDetail("Source", source));
				}

				report.addSection("Missing required dependency for " + this.type, details);
				continue;
			}

			Version version = required.version();

			if (!predicate.test(version)) {
				List<ReportDetail> details = Utils.mutableListOf(
						new ReportDetail(this.type.nameUpper, metadata.blame()),
						new ReportDetail("Dependency", required.blame()),
						new ReportDetail("Allowed Versions", predicate.toString())
				);

				String source = dependency.source();
				if (source != null) {
					details.add(new ReportDetail("Source", source));
				}

				report.addSection("Incorrect dependency for " + this.type, details);
			}
		}
	}

	private void checkIncompatibilities(Metadata metadata, ProblemReport report) {
		incompatibilities: for (Incompatibility incompatibility : metadata.incompatibilities().values()) {
			for (Condition condition : incompatibility.conditions()) {
				if (!condition.matches(this.context)) {
					continue incompatibilities;
				}
			}

			String incompatId = incompatibility.id();
			TypedMetadata incompatible = this.getTyped(incompatId);
			if (incompatible == null)
				continue;

			VersionPredicate predicate = incompatibility.predicate();
			Version version = incompatible.metadata.version();

			if (predicate.test(version)) {
				report.addSection(
						"Incompatible " + incompatible.type + " is present",
						new ReportDetail(this.type.nameUpper, metadata.blame()),
						new ReportDetail("Incompatible " + incompatible.type.nameUpper, incompatible.metadata.blame()),
						new ReportDetail("Incompatible Versions", predicate.toString()),
						new ReportDetail("Reason for Incompatibility", incompatibility.reason())
				);
			}
		}
	}

	@Nullable
	private Metadata get(String id) {
		TypedMetadata typed = this.getTyped(id);
		return typed == null ? null : typed.metadata;
	}

	@Nullable
	private TypedMetadata getTyped(String id) {
		return this.metadata.get(id);
	}

	public static DependencyChecker forPlugins(Map<String, LoadablePlugin> plugins) {
		Map<String, TypedMetadata> metadata = new HashMap<>();
		plugins.forEach((id, plugin) -> metadata.put(id, TypedMetadata.plugin(plugin)));
		return new DependencyChecker(metadata, LoadableType.PLUGIN);
	}

	public static DependencyChecker forMods(Map<String, LoadedPlugin> plugins, Map<String, LoadableMod> mods) {
		Map<String, TypedMetadata> metadata = new HashMap<>();
		plugins.forEach((id, plugin) -> metadata.put(id, TypedMetadata.plugin(plugin)));
		mods.forEach((id, mod) -> metadata.put(id, TypedMetadata.mod(mod)));
		return new DependencyChecker(metadata, LoadableType.MOD);
	}

	public static final class TypedMetadata {
		public final Metadata metadata;
		public final LoadableType type;

		public TypedMetadata(Metadata metadata, LoadableType type) {
			this.metadata = metadata;
			this.type = type;
		}

		public static TypedMetadata plugin(LoadablePlugin plugin) {
			return new TypedMetadata(plugin.metadata, LoadableType.PLUGIN);
		}

		public static TypedMetadata plugin(LoadedPlugin plugin) {
			return new TypedMetadata(plugin.representation.metadata(), LoadableType.PLUGIN);
		}

		public static TypedMetadata mod(LoadableMod mod) {
			return new TypedMetadata(mod.metadata, LoadableType.MOD);
		}
	}

	private final class ConditionContext implements Condition.Context {
		@Override
		public boolean isPluginPresent(String id) {
			TypedMetadata metadata = DependencyChecker.this.metadata.get(id);
			return metadata != null && metadata.type == LoadableType.PLUGIN;
		}

		@Override
		public boolean isModPresent(String id) {
			TypedMetadata metadata = DependencyChecker.this.metadata.get(id);
			return metadata != null && metadata.type == LoadableType.MOD;
		}
	}
}
