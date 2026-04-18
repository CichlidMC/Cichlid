package fish.cichlidmc.cichlid.impl.metadata;

import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.api.metadata.component.Dependency;
import fish.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.composite.JsonObject;

import java.util.Map;

public final class PluginMetadata extends MetadataImpl {
	public final String className;

	public PluginMetadata(String id, String name, ModVersion version, String description, Map<String, String> credits,
	                      Map<String, ModVersion> provides, Map<String, Dependency> dependencies,
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
