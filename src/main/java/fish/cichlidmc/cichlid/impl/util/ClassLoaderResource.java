package fish.cichlidmc.cichlid.impl.util;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClassLoaderResource<T> {
	private final T boot;
	private final Map<ClassLoader, T> map;
	private final Function<@Nullable ClassLoader, T> factory;

	public ClassLoaderResource(Function<@Nullable ClassLoader, T> factory) {
		this.factory = factory;
		this.boot = factory.apply(null);
		this.map = Collections.synchronizedMap(new HashMap<>());
	}

	public ClassLoaderResource(Supplier<T> factory) {
		this(_ -> factory.get());
	}

	public T get(@Nullable ClassLoader loader) {
		return loader == null ? this.boot : this.map.computeIfAbsent(loader, this.factory);
	}
}
