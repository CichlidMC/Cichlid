package fish.cichlidmc.cichlid.api.loaded;

import fish.cichlidmc.cichlid.api.metadata.Metadata;

/**
 * Something that can be loaded by Cichlid. Either a {@link Mod} or a {@link Plugin}.
 * <p>
 * Mods and plugins share some common attributes, which are made accessible here.
 */
public sealed interface Loadable permits Mod, Plugin {
	Metadata metadata();
}
