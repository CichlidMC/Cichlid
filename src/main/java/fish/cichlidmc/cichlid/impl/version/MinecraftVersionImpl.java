package fish.cichlidmc.cichlid.impl.version;

import fish.cichlidmc.cichlid.api.version.MinecraftVersion.NonRelease;
import fish.cichlidmc.cichlid.api.version.MinecraftVersion.Release;
import fish.cichlidmc.cichlid.api.version.MinecraftVersion.Standard;

import java.util.Comparator;
import java.util.Optional;

public final class MinecraftVersionImpl {
	private static final Comparator<Release> releaseComparator =
			Comparator.comparingInt(Release::year)
					.thenComparingInt(Release::drop)
					.thenComparingInt(Release::patch);

	private static final Comparator<NonRelease> nonReleaseComparator =
			Comparator.comparing(NonRelease::release, releaseComparator)
					.thenComparing(NonRelease::type)
					.thenComparing(NonRelease::number);

	public static Optional<? extends Standard> parseStandard(String string) {
		String trimmed = string.trim();
		String[] split = trimmed.split("-");

		// should always be either 1 or 3 sections (26.1, 26.1-snapshot-1)
		if (split.length != 1 && split.length != 3)
			return Optional.empty();

		Optional<Release> release = parseRelease(split[0]);
		if (release.isEmpty() || split.length == 1)
			return release;

		NonRelease.Type type = switch (split[1]) {
			case "snapshot" -> NonRelease.Type.SNAPSHOT;
			case "pre" -> NonRelease.Type.PRE_RELEASE;
			case "rc" -> NonRelease.Type.RELEASE_CANDIDATE;
			default -> null;
		};

		if (type == null) {
			return Optional.empty();
		}

		try {
			int number = Integer.parseInt(split[2]);
			return release.map(r -> new NonRelease(r, number, type));
		} catch (IllegalArgumentException _) { // includes NumberFormatException
			return Optional.empty();
		}
	}

	@SuppressWarnings("DataFlowIssue") // IDEA says the final cast here can fail, but that should be impossible
	public static int compare(Standard first, Standard second) {
		return switch (first) {
			// both the same
			case Release r1 when second instanceof Release r2 -> releaseComparator.compare(r1, r2);
			case NonRelease n1 when second instanceof NonRelease n2 -> nonReleaseComparator.compare(n1, n2);
			// mixed
			case Release release -> compareMixed(release, (NonRelease) second);
			default -> -compareMixed((Release) second, (NonRelease) first);
		};
	}

	private static int compareMixed(Release release, NonRelease nonRelease) {
		int byRelease = releaseComparator.compare(release, nonRelease.release());
		return byRelease != 0 ? byRelease : 1;
	}

	// 26.1, 26.1.1
	private static Optional<Release> parseRelease(String string) {
		String[] split = string.split("\\.");
		if (split.length == 1 || split.length > 3)
			return Optional.empty();

		int[] components = new int[3];
		for (int i = 0; i < split.length; i++) {
			try {
				components[i] = Integer.parseInt(split[i]);
			} catch (NumberFormatException _) {
				return Optional.empty();
			}
		}

		try {
			// patch will default to 0 if not present
			return Optional.of(new Release(components[0], components[1], components[2]));
		} catch (IllegalArgumentException _) {
			return Optional.empty();
		}
	}
}
