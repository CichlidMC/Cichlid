package io.github.cichlidmc.cichlid.impl.loading.mod;

import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadedMod;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public final class ClasspathLoadableMod extends LoadableMod {
	private final URI metadataLocation;

	public ClasspathLoadableMod(ModMetadata metadata, URI metadataLocation) {
		super(metadata, metadataLocation.toString());
		this.metadataLocation = metadataLocation;
	}

	@Override
	@SuppressWarnings("resource") // needs to stay open for access
	public LoadedMod load() throws IOException {
		try {
			// Paths.get(uri) throws if the filesystem isn't open.
			// but newFileSystem throws if the filesystem *is* open.
			FileSystems.newFileSystem(this.metadataLocation, Collections.emptyMap());
		} catch (FileSystemAlreadyExistsException | IllegalArgumentException ignored) {
			// catch IllegalArgumentException, since for some reason UnixFileSystem
			// throws on newFileSystem for any path besides the root.
		}

		Path resourcesRoot = Paths.get(this.metadataLocation).getParent();
		return LoadedMod.create(resourcesRoot);
	}
}
