package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.transformer.lookup.ClassLookup;
import net.neoforged.art.api.ClassProvider;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.internal.RenamingTransformer;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.Nullable;

public final class MinecraftRemapper {
	private final IMappingFile mappings;

	public MinecraftRemapper(IMappingFile mappings) {
		this.mappings = NoopMappingFile.INSTANCE;
	}

	@Nullable
	public RemappedClass remap(ClassLookup lookup, String name, byte[] bytes) {
		// this is super inefficient, but it's Fine:tm:
		TrackedMappingFile tracker = new TrackedMappingFile(this.mappings);
		ClassProvider provider = new DualClassProvider(ClassProvider.builder().addClass(name, bytes).build(), lookup);

		Transformer transformer = new RenamingTransformer(provider, tracker, message -> {}, false);

		Transformer.ClassEntry entry = Transformer.ClassEntry.create(name + ".class", Transformer.Entry.STABLE_TIMESTAMP, bytes);
		try {
			Transformer.ClassEntry processed = transformer.process(entry);
			return tracker.remappedAnything() ? RemappedClass.of(processed) : null;
		} catch (CancelRemappingException ignored) {
			System.out.println("Remapping of " + name + " was cancelled");
			return null;
		}
	}
}
