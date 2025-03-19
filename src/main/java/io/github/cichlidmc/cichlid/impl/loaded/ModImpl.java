package io.github.cichlidmc.cichlid.impl.loaded;

import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.loaded.Plugin;
import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;

import java.nio.file.Path;
import java.util.Optional;

public final class ModImpl implements Mod {
	private final ModMetadata metadata;
	private final Plugin loader;
	private final Optional<Path> resources;

	public ModImpl(ModMetadata metadata, Plugin loader, Optional<Path> resources) {
		this.metadata = metadata;
		this.loader = loader;
		this.resources = resources;
	}

	@Override
	public ModMetadata metadata() {
		return this.metadata;
	}

	@Override
	public Plugin loader() {
		return this.loader;
	}

	@Override
	public Optional<Path> resources() {
		return this.resources;
	}
}
