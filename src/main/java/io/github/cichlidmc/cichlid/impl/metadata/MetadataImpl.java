package io.github.cichlidmc.cichlid.impl.metadata;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.metadata.component.DependencyImpl;
import io.github.cichlidmc.cichlid.impl.metadata.component.IncompatibilityImpl;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class MetadataImpl implements Metadata {
	protected final String id;
	protected final String name;
	protected final Version version;
	protected final String description;
	protected final Map<String, String> credits;
	protected final Map<String, Version> provides;
	protected final Map<String, Dependency> dependencies;
	protected final Map<String, Incompatibility> incompatibilities;

	MetadataImpl(String id, String name, Version version, String description, Map<String, String> credits,
				 Map<String, Version> provides, Map<String, Dependency> dependencies, Map<String, Incompatibility> incompatibilities) {
		if (!Metadata.isValidId(id)) {
			throw new IllegalArgumentException("Invalid ID: " + id);
		}

		this.id = id;
		this.name = name;
		this.version = version;
		this.description = description;
		this.credits = Collections.unmodifiableMap(credits);
		this.provides = Collections.unmodifiableMap(provides);
		this.dependencies = Collections.unmodifiableMap(dependencies);
		this.incompatibilities = Collections.unmodifiableMap(incompatibilities);
	}

	MetadataImpl(Metadata base) {
		this(base.id(), base.name(), base.version(), base.description(), base.credits(), base.provides(), base.dependencies(), base.incompatibilities());
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public Version version() {
		return this.version;
	}

	@Override
	public String description() {
		return this.description;
	}

	@Override
	public Map<String, String> credits() {
		return this.credits;
	}

	@Override
	public Map<String, Version> provides() {
		return this.provides;
	}

	@Override
	public Map<String, Dependency> dependencies() {
		return this.dependencies;
	}

	@Override
	public Map<String, Incompatibility> incompatibilities() {
		return this.incompatibilities;
	}

	static MetadataImpl fromJson(JsonObject json) throws JsonException {
		String id = json.get("id").asString().value();
		String name = json.get("name").asString().value();
		String versionString = json.get("version").asString().value();
		Version version = Version.of(versionString);
		String description = json.getOptional("description").map(value -> value.asString().value()).orElse("");

		Map<String, String> credits = new LinkedHashMap<>();
		json.getOptional("credits").ifPresent(
				value -> value.asObject().forEach((k, v) -> credits.put(k, v.asString().value()))
		);

		Map<String, Version> provides = new HashMap<>();
		json.getOptional("provides").ifPresent(
				value -> value.asObject().forEach((k, v) -> provides.put(k, Version.of(v.asString().value())))
		);

		Map<String, Dependency> dependencies = new HashMap<>();
		json.getOptional("dependencies").ifPresent(
				value -> value.asObject().forEach((k, v) -> dependencies.put(k, DependencyImpl.parse(k, v.asObject())))
		);

		Map<String, Incompatibility> incompatibilities = new HashMap<>();
		json.getOptional("incompatibilities").ifPresent(
				value -> value.asObject().forEach((k, v) -> incompatibilities.put(k, IncompatibilityImpl.parse(k, v.asObject())))
		);

		return new MetadataImpl(id, name, version, description, credits, provides, dependencies, incompatibilities);
	}
}
