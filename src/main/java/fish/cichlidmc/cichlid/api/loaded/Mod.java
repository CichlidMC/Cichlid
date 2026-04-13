package fish.cichlidmc.cichlid.api.loaded;

import fish.cichlidmc.cichlid.api.metadata.ModMetadata;
import fish.cichlidmc.cichlid.impl.loaded.ModImpl;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A mod loaded by a Cichlid plugin.
 */
public sealed interface Mod extends Loadable permits ModImpl {
	@Override
	ModMetadata metadata();

	/**
	 * The plugin that loaded this mod.
	 */
	Plugin loader();

	/**
	 * Root path to resources provided by this mod. May be empty if none exist.
	 * For a jar file, this would be the root.
	 */
	Optional<Path> resources();
}
