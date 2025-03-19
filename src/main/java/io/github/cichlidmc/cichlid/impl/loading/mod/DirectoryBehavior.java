package io.github.cichlidmc.cichlid.impl.loading.mod;

import io.github.cichlidmc.cichlid.api.version.Version;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

@FunctionalInterface
interface DirectoryBehavior {
	DirectoryBehavior IGNORE = (dir, candidates, mcVersion) -> FileVisitResult.SKIP_SUBTREE;
	DirectoryBehavior SEARCH = (dir, candidates, mcVersion) -> FileVisitResult.CONTINUE;
	DirectoryBehavior MOD = (dir, candidates, mcVersion) -> {
		candidates.add(dir);
		return FileVisitResult.SKIP_SUBTREE;
	};

	DirectoryBehavior DEFAULT = SEARCH;

	FileVisitResult apply(Path dir, Set<Path> candidates, Version mcVersion);

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
				VersionPredicate parsed = VersionPredicate.parse(predicate);
				return new ConditionalSearch(parsed);
			} catch (VersionPredicateSyntaxException e) {
				throw new IllegalStateException("Directory has invalid version predicate: " + dir, e);
			}
		} else {
			throw new IllegalStateException("Directory has an invalid load_behavior of '" + behavior + "': " + dir);
		}
	}

	class ConditionalSearch implements DirectoryBehavior {
		private final VersionPredicate predicate;

		public ConditionalSearch(VersionPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		public FileVisitResult apply(Path dir, Set<Path> candidates, Version mcVersion) {
			return this.predicate.test(mcVersion) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
		}
	}
}
