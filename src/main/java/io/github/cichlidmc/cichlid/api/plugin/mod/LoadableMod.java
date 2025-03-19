package io.github.cichlidmc.cichlid.api.plugin.mod;

import io.github.cichlidmc.cichlid.api.metadata.ModMetadata;

import java.io.IOException;

/**
 * A mod ready for loading by Cichlid. Plugins should extend this class.
 */
public abstract class LoadableMod {
	public final ModMetadata metadata;

	/**
	 * Description of where this mod comes from, usually a file path
	 */
	public final String source;

	protected LoadableMod(ModMetadata metadata, String source) {
		this.metadata = metadata;
		this.source = source;
	}

	/**
	 * Called after Cichlid has determined that this mod will definitely be loaded into the game.
	 * Load this mod. This may involve generating cache files, opening a file system, etc.
	 */
	public abstract LoadedMod load() throws IOException;
}
