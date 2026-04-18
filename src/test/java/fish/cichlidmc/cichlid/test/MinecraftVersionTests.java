package fish.cichlidmc.cichlid.test;

import fish.cichlidmc.cichlid.api.version.MinecraftVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class MinecraftVersionTests {
	@Test
	public void run() {
		List<String> strings = List.of(
				"26.1-snapshot-1", "26.1-snapshot-2",
				"26.1-pre-1", "26.1-pre-2",
				"26.1-rc-1", "26.1-rc-2",
				"26.1",

				"26.1.1-snapshot-1", "26.1.1-snapshot-2",
				"26.1.1-pre-1", "26.1.1-pre-2",
				"26.1.1-rc-1", "26.1.1-rc-2",
				"26.1.1",

				"26.2-snapshot-1", "26.2-snapshot-2",
				"26.2-pre-1", "26.2-pre-2",
				"26.2-rc-1", "26.2-rc-2",
				"26.2",

				"27.1-snapshot-1", "27.1-snapshot-2",
				"27.1-pre-1", "27.1-pre-2",
				"27.1-rc-1", "27.1-rc-2",
				"27.1"
		);

		List<? extends MinecraftVersion.Standard> versions = strings.stream()
				.map(MinecraftVersion::parseStandard)
				.map(Optional::orElseThrow)
				.toList();

		List<? extends MinecraftVersion.Standard> copy = new ArrayList<>(versions);
		Random random = new Random(42);

		for (int i = 0; i < 10; i++) {
			Collections.shuffle(copy, random);
			copy.sort(Comparator.naturalOrder());
			Assertions.assertEquals(versions, copy);
		}

		Assertions.assertEquals(strings.size(), versions.size());

		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			MinecraftVersion.Standard version = versions.get(i);
			Assertions.assertEquals(string, version.toString());
		}
	}
}
