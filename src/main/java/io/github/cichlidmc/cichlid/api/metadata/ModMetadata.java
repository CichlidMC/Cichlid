package io.github.cichlidmc.cichlid.api.metadata;

import io.github.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import io.github.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Mod-specific metadata.
 * For plugins trying to create an instance of this class, see {@link ModMetadataBuilder}.
 */
@ApiStatus.NonExtendable
public interface ModMetadata extends Metadata {
	/**
	 * Entrypoints. See {@link Entrypoints} for information.
	 */
	Entrypoints entrypoints();
}
