package io.github.cichlidmc.cichlid.api.plugin.mod;

import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.impl.loaded.LoadedModImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;

/**
 * A mod loaded by a plugin, ready for Cichlid to handle it.
 */
@ApiStatus.NonExtendable
public interface LoadedMod {
	/**
	 * Due to Java API restrictions, all mods must end up as a JarFile for class loading.
	 * This could be the mod file directly, or it could be generated from another format.
	 * If no jar is provided, no classes will be loadable from this mod.
	 */
	Optional<JarFile> jar();

	/**
	 * Resources that will be accessible to mods through {@link Mod#resources()}.
	 */
	Optional<Path> resources();

	/**
	 * Create a LoadedMod from a JarFile, getting resources from within it.
	 */
	static LoadedMod create(JarFile jar) throws IOException {
		return LoadedModImpl.create(jar);
	}

	/**
	 * Create a LoadedMod with resources from a custom location.
	 */
	static LoadedMod create(JarFile jar, @Nullable Path resources) {
		return LoadedModImpl.create(jar, resources);
	}

	/**
	 * Create a LoadedMod that will only contain resources, no classes.
	 */
	static LoadedMod create(Path resources) {
		return LoadedModImpl.create(resources);
	}
}
