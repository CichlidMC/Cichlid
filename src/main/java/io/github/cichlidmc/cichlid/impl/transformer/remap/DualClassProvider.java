package io.github.cichlidmc.cichlid.impl.transformer.remap;

import net.neoforged.art.api.ClassProvider;

import java.io.IOException;
import java.util.Optional;

public final class DualClassProvider implements ClassProvider {
	private final ClassProvider first;
	private final ClassProvider second;

	public DualClassProvider(ClassProvider first, ClassProvider second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public Optional<? extends IClassInfo> getClass(String cls) {
		Optional<? extends IClassInfo> first = this.first.getClass(cls);
		return first.isPresent() ? first : this.second.getClass(cls);
	}

	@Override
	public void close() throws IOException {
		this.first.close();
		this.second.close();
	}
}
