package io.github.cichlidmc.cichlid.api.loaded;

import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A mod loaded by a Cichlid plugin.
 */
@ApiStatus.NonExtendable
public interface Mod {
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
