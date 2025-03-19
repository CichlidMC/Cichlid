package io.github.cichlidmc.cichlid.impl.metadata;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;

import java.util.Map;

public final class PluginMetadata extends MetadataImpl {
	public final String className;

	public PluginMetadata(String id, String name, Version version, String description, Map<String, String> credits,
						  Map<String, Version> provides, Map<String, Dependency> dependencies,
						  Map<String, Incompatibility> incompatibilities, String className) {
		super(id, name, version, description, credits, provides, dependencies, incompatibilities);
		this.className = className;
	}

	private PluginMetadata(Metadata base, String className) {
		super(base);
		this.className = className;
	}

	public static PluginMetadata fromJson(JsonObject json) throws JsonException {
		MetadataImpl metadata = MetadataImpl.fromJson(json);
		String className = json.get("class_name").asString().value();
		return new PluginMetadata(metadata, className);
	}
}
