package fish.cichlidmc.cichlid.api.metadata;

import fish.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import fish.cichlidmc.cichlid.api.plugin.ModMetadataBuilder;
import fish.cichlidmc.cichlid.impl.metadata.ModMetadataImpl;

/// Mod-specific metadata.
/// For plugins trying to create an instance of this class, see [ModMetadataBuilder].
public sealed interface ModMetadata extends Metadata permits ModMetadataImpl {
	/// Entrypoints. See [Entrypoints] for information.
	Entrypoints entrypoints();
}
