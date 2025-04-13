package io.github.cichlidmc.cichlid.impl.transformer.lookup;

import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClassLookupStorage {
	private final CichlidTransformer transformer;
	private final ClassLookups bootLookups;
	private final Map<ClassLoader, ClassLookups> lookups;

	public ClassLookupStorage(CichlidTransformer transformer) {
		this.transformer = transformer;
		this.bootLookups = new ClassLookups(transformer, null);
		this.lookups = Collections.synchronizedMap(new HashMap<>());
	}

	public ClassLookups get(@Nullable ClassLoader loader) {
		return loader == null ? this.bootLookups : this.lookups.computeIfAbsent(loader, this::create);
	}

	private ClassLookups create(ClassLoader loader) {
		return new ClassLookups(this.transformer, loader);
	}
}
