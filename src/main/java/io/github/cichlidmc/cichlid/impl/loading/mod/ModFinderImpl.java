package io.github.cichlidmc.cichlid.impl.loading.mod;

import io.github.cichlidmc.cichlid.api.Cichlid;
import io.github.cichlidmc.cichlid.api.plugin.ModFinder;
import io.github.cichlidmc.cichlid.api.version.Version;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class ModFinderImpl implements ModFinder {
	public final Set<Path> files = new HashSet<>();
	public final Set<Path> directories = new HashSet<>();

	@Override
	public void addFile(Path file) {
		if (this.directories.contains(file)) {
			throw new IllegalArgumentException("Cannot register path as both a mod and directory: " + file);
		}

		this.files.add(file);
	}

	@Override
	public void addDirectory(Path directory) {
		if (this.files.contains(directory)) {
			throw new IllegalArgumentException("Cannot register path as both a mod and directory: " + directory);
		}
		if (!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Path is not a directory: " + directory);
		}

		this.directories.add(directory);
	}

	public Set<Path> find() throws IOException {
		Set<Path> mods = new HashSet<>(this.files);
		Version mcVersion = Cichlid.mcVersion();

		for (Path directory : this.directories) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					mods.add(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return DirectoryBehavior.of(dir).apply(dir, mods, mcVersion);
				}
			});
		}

		return mods;
	}
}
