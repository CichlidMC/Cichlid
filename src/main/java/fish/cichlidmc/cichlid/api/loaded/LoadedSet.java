package fish.cichlidmc.cichlid.api.loaded;

import fish.cichlidmc.cichlid.impl.util.LoadedSetImpl;

import java.util.Collection;
import java.util.Optional;

/// Generic interface representing a loaded set of mods or plugins.
public sealed interface LoadedSet<T> extends Collection<T> permits LoadedSetImpl {
	boolean isLoaded(String id);

	Optional<T> get(String id);

	T getOrThrow(String id);
}
