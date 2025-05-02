package fish.cichlidmc.cichlid.api.metadata;

import fish.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import fish.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
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
