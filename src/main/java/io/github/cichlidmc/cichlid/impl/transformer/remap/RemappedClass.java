package io.github.cichlidmc.cichlid.impl.transformer.remap;

import net.neoforged.art.api.Transformer;

public final class RemappedClass {
	public final String name;
	public final byte[] bytes;

	public RemappedClass(String name, byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}

	public static RemappedClass of(Transformer.ClassEntry entry) {
		return new RemappedClass(entry.getClassName(), entry.getData());
	}
}
