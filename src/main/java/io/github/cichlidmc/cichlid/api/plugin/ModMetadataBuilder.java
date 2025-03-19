package io.github.cichlidmc.cichlid.api.plugin;

import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.impl.metadata.ModMetadataBuilderImpl;
import io.github.cichlidmc.cichlid.impl.metadata.ModMetadataImpl;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builder for a {@link ModMetadata}. All fields are required unless otherwise noted.
 */
@ApiStatus.NonExtendable
public interface ModMetadataBuilder {
	/**
	 * @throws IllegalArgumentException if the ID is not valid
	 */
	ModMetadataBuilder id(String id);

	ModMetadataBuilder name(String name);

	ModMetadataBuilder version(Version version);

	ModMetadataBuilder description(String description);

	/**
	 * Optional. Adds an entry to the entrypoints with the given key.
	 */
	ModMetadataBuilder entrypoint(String key, String value);

	/**
	 * Optional. Adds an entry to the credits.
	 */
	ModMetadataBuilder credit(String name, String role);

	/**
	 * Optional. Adds a mod that this metadata provides.
	 */
	ModMetadataBuilder provides(String id, Version version);

	/**
	 * Optional. Adds a dependency to this metadata.
	 */
	ModMetadataBuilder dependency(Dependency dependency);

	/**
	 * Optional. Adds an incompatibility to this metadata.
	 */
	ModMetadataBuilder incompatibility(Incompatibility incompatibility);

	/**
	 * Build this builder into a {@link ModMetadata}.
	 * @throws IllegalArgumentException if any required fields were not set
	 */
	ModMetadata build();

	/**
	 * Create a new empty builder.
	 */
	static ModMetadataBuilder create() {
		return ModMetadataBuilderImpl.create();
	}

	/**
	 * Parse mod metadata from the given JSON. See {@link TinyJson} for getting an instance of a {@link JsonObject}.
	 * @throws JsonException if the JSON is not a valid {@link ModMetadata}.
	 */
	static ModMetadata fromJson(JsonObject json) throws JsonException {
		return ModMetadataImpl.fromJson(json);
	}
}
