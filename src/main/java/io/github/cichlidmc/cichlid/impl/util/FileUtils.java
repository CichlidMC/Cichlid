package io.github.cichlidmc.cichlid.impl.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
	public static boolean isDisabled(Path file) {
		String name = file.getFileName().toString();
		return name.startsWith(".") || name.endsWith(".disabled");
	}

	public static boolean nameEndsWith(Path path, String suffix) {
		return path.getFileName().toString().endsWith(suffix);
	}

	public static File toFileOrNull(Path path) {
		try {
			return path.toFile();
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@SuppressWarnings("RedundantCast") // it's not redundant, compilation fails without it
	public static FileSystem openJar(Path path) throws IOException {
		return FileSystems.newFileSystem(path, (ClassLoader) null);
	}

	public static void walkFiles(Path start, FileConsumer consumer) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				consumer.accept(file);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public interface FileConsumer {
		void accept(Path file) throws IOException;
	}
}
