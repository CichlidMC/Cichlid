package fish.cichlidmc.cichlid.impl.loading.mod;

import fish.cichlidmc.cichlid.api.version.MinecraftVersion;
import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

@FunctionalInterface
interface DirectoryBehavior {
	DirectoryBehavior IGNORE = (_, _, _) -> FileVisitResult.SKIP_SUBTREE;
	DirectoryBehavior SEARCH = (_, _, _) -> FileVisitResult.CONTINUE;
	DirectoryBehavior MOD = (dir, candidates, _) -> {
		candidates.add(dir);
		return FileVisitResult.SKIP_SUBTREE;
	};

	DirectoryBehavior DEFAULT = SEARCH;

	FileVisitResult apply(Path dir, Set<Path> candidates, MinecraftVersion mcVersion);

	static DirectoryBehavior of(Path dir) throws IOException {
		Path propertiesFile = dir.resolve("cichlid.properties");
		if (!Files.exists(propertiesFile))
			return DEFAULT;

		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesFile));
		String behavior = properties.getProperty("load_behavior");
		if (behavior == null)
			return DEFAULT;

		if (behavior.equals("mod")) {
			return MOD;
		} else if (behavior.equals("ignore")) {
			return IGNORE;
		} else if (behavior.equals("search")) {
			String predicate = properties.getProperty("predicate");
			if (predicate == null)
				return SEARCH;

			try {
				Predicate<MinecraftVersion> parsed = MinecraftVersion.parsePredicate(predicate);
				return new ConditionalSearch(parsed);
			} catch (VersionPredicateSyntaxException e) {
				throw new IllegalStateException("Directory has invalid version predicate: " + dir, e);
			}
		} else {
			throw new IllegalStateException("Directory has an invalid load_behavior of '" + behavior + "': " + dir);
		}
	}

	class ConditionalSearch implements DirectoryBehavior {
		private final Predicate<MinecraftVersion> predicate;

		public ConditionalSearch(Predicate<MinecraftVersion> predicate) {
			this.predicate = predicate;
		}

		@Override
		public FileVisitResult apply(Path dir, Set<Path> candidates, MinecraftVersion mcVersion) {
			return this.predicate.test(mcVersion) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
		}
	}
}
