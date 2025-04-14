package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.remap.ReadingClassProvider;
import net.neoforged.art.api.ClassProvider;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.internal.RenamingTransformer;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.Nullable;

public final class RuntimeMinecraftRemapper {
	private final IMappingFile mappings;

	public RuntimeMinecraftRemapper(IMappingFile mappings) {
		this.mappings = mappings;
	}

	@Nullable
	public RemappedClass remap(ReadingClassProvider provider, String name, byte[] bytes) {
		// this is super inefficient, but it's Fine:tm:
		TrackedMappingFile tracker = new TrackedMappingFile(this.mappings);
		ClassProvider fullProvider = new DualClassProvider(ClassProvider.builder().addClass(name, bytes).build(), provider);

		Transformer transformer = new RenamingTransformer(fullProvider, tracker, message -> {}, false);

		Transformer.ClassEntry entry = Transformer.ClassEntry.create(name + ".class", Transformer.Entry.STABLE_TIMESTAMP, bytes);
		Transformer.ClassEntry processed = transformer.process(entry);
		return tracker.remappedAnything() ? RemappedClass.of(processed) : null;
	}
}
