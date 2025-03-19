package io.github.cichlidmc.cichlid.impl.loading;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import io.github.cichlidmc.cichlid.impl.loading.mod.ClasspathLoadableMod;
import io.github.cichlidmc.cichlid.impl.loading.plugin.LoadablePlugin;
import io.github.cichlidmc.cichlid.impl.loading.plugin.PluginLoader;
import io.github.cichlidmc.cichlid.impl.loading.plugin.builtin.StandardJarPlugin;
import io.github.cichlidmc.cichlid.impl.metadata.PluginMetadata;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;
import io.github.cichlidmc.cichlid.impl.util.Either;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.JsonValue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ClasspathLoadableFinder<T, M extends Metadata> {
	private final String metadataFileName;
	private final SharedLoading.MetadataParser<M> parser;
	private final Function<T, M> metadataGetter;
	private final Function<T, String> sourceGetter;
	private final Factory<T, M> factory;
	private final LoadableType type;

	public ClasspathLoadableFinder(String metadataFileName, SharedLoading.MetadataParser<M> parser, Function<T, M> metadataGetter,
								   Function<T, String> sourceGetter, Factory<T, M> factory, LoadableType type) {
		this.metadataFileName = metadataFileName;
		this.parser = parser;
		this.metadataGetter = metadataGetter;
		this.sourceGetter = sourceGetter;
		this.factory = factory;
		this.type = type;
	}

	public void find(Map<String, T> map, ProblemReport report) throws IOException {
		Enumeration<URL> metadataFiles = this.getClass().getClassLoader().getResources(this.metadataFileName);

		while (metadataFiles.hasMoreElements()) {
			URL metadataFile = metadataFiles.nextElement();
			try {
				URI uri = metadataFile.toURI();
				Either<M, JsonException> either = this.tryParse(uri);
				if (either.isRight()) {
					report.addSection(
							"Failed to read metadata from on classpath",
							new ReportDetail("Location", uri.toString()),
							new ReportDetail("Error", either.right().getMessage())
					);

					continue;
				}

				M metadata = either.left();
				String id = metadata.id();

				if (!this.handleDuplicate(map, metadata, report, uri.toString())) {
					map.put(id, this.factory.create(metadata, uri));
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Either<M, JsonException> tryParse(URI uri) throws IOException {
		try {
			JsonValue json = TinyJson.fetch(uri);
			M metadata = this.parser.parse(json.asObject());
			return Either.left(metadata);
		} catch (JsonException e) {
			return Either.right(e);
		}
	}

	// nearly identical logic here and in both loader classes. deduplicating it is a mess though, it's finetm
	private boolean handleDuplicate(Map<String, T> map, M metadata, ProblemReport report, String location) {
		T existing = map.get(metadata.id());
		if (existing == null)
			return false;

		List<ReportDetail> details = Utils.mutableListOf(
				new ReportDetail("Name", metadata.name()),
				new ReportDetail("ID", metadata.id()),
				new ReportDetail("Location", location)
		);

		M existingMetadata = this.metadataGetter.apply(existing);

		if (!metadata.name().equals(existingMetadata.name())) {
			details.add(new ReportDetail("Other Name", existingMetadata.name()));
		}

		String source = this.sourceGetter.apply(existing);
		details.add(new ReportDetail("Other Location", source));

		report.addSection("Duplicate " + this.type + " found on classpath", details);
		return true;
	}

	public static ClasspathLoadableFinder<LoadablePlugin, PluginMetadata> forPlugins() {
		return new ClasspathLoadableFinder<>(
				PluginLoader.METADATA, PluginMetadata::fromJson, plugin -> plugin.metadata,
				plugin -> plugin.source, LoadablePlugin.Classpath::new, LoadableType.PLUGIN
		);
	}

	public static ClasspathLoadableFinder<LoadableMod, ModMetadata> forMods() {
		return new ClasspathLoadableFinder<>(
				StandardJarPlugin.MOD_METADATA, ModMetadataBuilder::fromJson, mod -> mod.metadata,
				mod -> mod.source, ClasspathLoadableMod::new, LoadableType.MOD
		);
	}

	@FunctionalInterface
	public interface Factory<T, M extends Metadata> {
		T create(M metadata, URI metadataLocation);
	}
}
