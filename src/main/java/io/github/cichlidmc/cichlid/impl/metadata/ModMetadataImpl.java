package io.github.cichlidmc.cichlid.impl.metadata;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.metadata.component.EntrypointsImpl;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;

import java.util.Map;

public final class ModMetadataImpl extends MetadataImpl implements ModMetadata {
	private final Entrypoints entrypoints;

	public ModMetadataImpl(String id, String name, Version version, String description,
						   Entrypoints entrypoints, Map<String, String> credits, Map<String, Version> provides,
						   Map<String, Dependency> dependencies, Map<String, Incompatibility> incompatibilities) {
		super(id, name, version, description, credits, provides, dependencies, incompatibilities);
		this.entrypoints = entrypoints;
	}

	private ModMetadataImpl(Metadata base, Entrypoints entrypoints) {
		super(base);
		this.entrypoints = entrypoints;
	}

	@Override
	public Entrypoints entrypoints() {
		return this.entrypoints;
	}

	public static ModMetadataImpl fromJson(JsonObject json) throws JsonException {
		MetadataImpl metadata = MetadataImpl.fromJson(json);

		Entrypoints entrypoints = json.getOptional("entrypoints")
				.map(JsonValue::asObject)
				.map(EntrypointsImpl::parse)
				.orElse(EntrypointsImpl.EMPTY);

		return new ModMetadataImpl(metadata, entrypoints);
	}
}
