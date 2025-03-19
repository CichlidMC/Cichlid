package io.github.cichlidmc.cichlid.api.metadata.component;

import io.github.cichlidmc.cichlid.api.mod.entrypoint.PreLaunchEntrypoint;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Entrypoints are a map of keys to arbitrary strings. How an entrypoint works is up to the implementor.
 * <p>
 * For example, the {@link PreLaunchEntrypoint} is specified with the "pre_launch" key. The value
 * is a class name that will be instantiated and invoked. An entrypoint does not need to be
 * a class, but that is a very common way to use them.
 */
@ApiStatus.NonExtendable
public interface Entrypoints {
	/**
	 * Get the list of entrypoints for the given key.
	 * Never returns null. If none are present, an empty list is returned.
	 */
	List<String> get(String key);

	/**
	 * Returns true if there are one or more entrypoints for the given key.
	 */
	boolean contains(String key);

	/**
	 * Invoke a consumer for each set of entrypoints.
	 */
	void forEach(BiConsumer<String, List<String>> consumer);
}
