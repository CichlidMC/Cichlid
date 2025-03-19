package io.github.cichlidmc.cichlid.api.loaded;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Optional;

/**
 * Generic interface representing a loaded set of mods or plugins.
 */
@ApiStatus.NonExtendable
public interface LoadedSet<T> extends Collection<T> {
	boolean isLoaded(String id);

	Optional<T> get(String id);

	T getOrThrow(String id);
}
