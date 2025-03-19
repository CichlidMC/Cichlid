package io.github.cichlidmc.cichlid.impl.loaded;

import io.github.cichlidmc.cichlid.api.plugin.mod.LoadedMod;
import io.github.cichlidmc.cichlid.impl.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;

public class LoadedModImpl implements LoadedMod {
	private final Optional<JarFile> jar;
	private final Optional<Path> resources;

	public LoadedModImpl(Optional<JarFile> jar, Optional<Path> resources) {
		this.jar = jar;
		this.resources = resources;
	}

	@Override
	public Optional<JarFile> jar() {
		return this.jar;
	}

	@Override
	public Optional<Path> resources() {
		return this.resources;
	}

	@SuppressWarnings("resource") // filesystem needs to stay open for resources
	public static LoadedMod create(JarFile jar) throws IOException {
		File asFile = new File(jar.getName());
		FileSystem fs = FileUtils.openJar(asFile.toPath());
		Path root = fs.getRootDirectories().iterator().next();
		return create(jar, root);
	}

	public static LoadedMod create(JarFile jar, @Nullable Path resources) {
		Optional<Path> optional = Optional.ofNullable(resources).filter(Files::exists);
		return new LoadedModImpl(Optional.of(jar), optional);
	}

	public static LoadedMod create(Path resources) {
		return new LoadedModImpl(Optional.empty(), Optional.of(resources));
	}
}
