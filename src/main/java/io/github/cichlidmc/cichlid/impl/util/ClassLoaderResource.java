package io.github.cichlidmc.cichlid.impl.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ClassLoaderResource<T> {
	private final T boot;
	private final Map<ClassLoader, T> map;
	private final Function<@Nullable ClassLoader, T> factory;

	public ClassLoaderResource(Function<@Nullable ClassLoader, T> factory) {
		this.factory = factory;
		this.boot = factory.apply(null);
		this.map = Collections.synchronizedMap(new HashMap<>());
	}

	public T get(@Nullable ClassLoader loader) {
		return loader == null ? this.boot : this.map.computeIfAbsent(loader, this.factory);
	}
}
