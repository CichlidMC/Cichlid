package io.github.cichlidmc.cichlid.api.plugin;

import java.nio.file.Path;

/**
 * Searches files and directories for mods that may be loaded by plugins.
 */
public interface ModFinder {
	/**
	 * Add a single file to be loaded as a mod. Could be a regular file or a directory.
	 * @throws IllegalArgumentException if the file has already been added as a directory
	 */
	void addFile(Path file);

	/**
	 * Add an entire directory to be searched for mods. All files contained within the directory will be searched.
	 * Subfolders will be searched recursively, abiding by a specified DirectoryBehavior if present.
	 * If any file within the directory is not a mod, mod loading will fail.
	 * @throws IllegalArgumentException if the path is not a directory, or if it was already registered as a single file
	 */
	void addDirectory(Path directory);
}
