package fish.cichlidmc.cichlid.impl.metadata;

import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.api.metadata.ModMetadata;
import fish.cichlidmc.cichlid.api.metadata.component.Dependency;
import fish.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import fish.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.impl.metadata.component.EntrypointsImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModMetadataBuilderImpl implements ModMetadataBuilder {
	@Nullable
	private String id;
	@Nullable
	private String name;
	@Nullable
	private ModVersion version;
	@Nullable
	private String description;

	private final Map<String, List<String>> entrypoints = new HashMap<>();
	private final Map<String, String> credits = new HashMap<>();
	private final Map<String, ModVersion> provides = new HashMap<>();
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
	public ModMetadataBuilder version(ModVersion version) {
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
		List<String> values = this.entrypoints.computeIfAbsent(key, _ -> new ArrayList<>());
		values.add(value);
		return this;
	}

	@Override
	public ModMetadataBuilder credit(String name, String role) {
		this.credits.put(name, role);
		return this;
	}

	@Override
	public ModMetadataBuilder provides(String id, ModVersion version) {
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
		String id = Objects.requireNonNull(this.id, "ID has not been set");
		String name = Objects.requireNonNull(this.name, "Name has not been set");
		ModVersion version = Objects.requireNonNull(this.version, "Version has not been set");
		String description = Objects.requireNonNull(this.description, "Description has not been set");

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
