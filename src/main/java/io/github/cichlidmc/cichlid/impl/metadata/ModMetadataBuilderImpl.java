package io.github.cichlidmc.cichlid.impl.metadata;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.metadata.component.EntrypointsImpl;
import io.github.cichlidmc.cichlid.impl.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMetadataBuilderImpl implements ModMetadataBuilder {
	private String id;
	private String name;
	private Version version;
	private String description;

	private final Map<String, List<String>> entrypoints = new HashMap<>();
	private final Map<String, String> credits = new HashMap<>();
	private final Map<String, Version> provides = new HashMap<>();
	private final Map<String, Dependency> dependencies = new HashMap<>();
	private final Map<String, Incompatibility> incompatibilities = new HashMap<>();

	@Override
	public ModMetadataBuilder id(String id) {
		if (!Metadata.isValidId(id)) {
			throw new IllegalArgumentException("Invalid ID: " + id);
		}

		this.id = id;
		return this;
	}

	@Override
	public ModMetadataBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public ModMetadataBuilder version(Version version) {
		this.version = version;
		return this;
	}

	@Override
	public ModMetadataBuilder description(String description) {
		this.description = description;
		return this;
	}

	@Override
	public ModMetadataBuilder entrypoint(String key, String value) {
		List<String> values = this.entrypoints.computeIfAbsent(key, $ -> new ArrayList<>());
		values.add(value);
		return this;
	}

	@Override
	public ModMetadataBuilder credit(String name, String role) {
		this.credits.put(name, role);
		return this;
	}

	@Override
	public ModMetadataBuilder provides(String id, Version version) {
		this.provides.put(id, version);
		return this;
	}

	@Override
	public ModMetadataBuilder dependency(Dependency dependency) {
		this.dependencies.put(dependency.id(), dependency);
		return this;
	}

	@Override
	public ModMetadataBuilder incompatibility(Incompatibility incompatibility) {
		this.incompatibilities.put(incompatibility.id(), incompatibility);
		return this;
	}

	@Override
	public ModMetadata build() {
		String id = Utils.getOrThrow(this.id, "ID has not been set");
		String name = Utils.getOrThrow(this.name, "Name has not been set");
		Version version = Utils.getOrThrow(this.version, "Version has not been set");
		String description = Utils.getOrThrow(this.description, "Description has not been set");

		return new ModMetadataImpl(
				id, name, version, description,
				new EntrypointsImpl(this.entrypoints),
				this.credits, this.provides, this.dependencies, this.incompatibilities
		);
	}

	public static ModMetadataBuilder create() {
		return new ModMetadataBuilderImpl();
	}
}
