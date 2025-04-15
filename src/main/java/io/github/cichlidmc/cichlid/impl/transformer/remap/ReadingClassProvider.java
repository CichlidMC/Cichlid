package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.util.Utils;
import net.neoforged.art.api.ClassProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ReadingClassProvider implements ClassProvider {
	@Nullable
	private final ClassLoader loader;
	private final Map<String, IClassInfo> cache;

	public ReadingClassProvider(@Nullable ClassLoader loader) {
		this.loader = loader;
		this.cache = Collections.synchronizedMap(new HashMap<>());
	}

	@Override
	public Optional<? extends IClassInfo> getClass(String cls) {
		return Optional.ofNullable(this.cache.computeIfAbsent(cls, this::compute));
	}

	@Override
	public void close() {
	}

	@Nullable
	private IClassInfo compute(String name) {
		return readClassInfo(this.loader, name);
	}

	@Nullable
	public static IClassInfo readClassInfo(@Nullable ClassLoader loader, String name) {
		byte[] bytes = Utils.readClassLoaderResource(loader, name + ".class");
		return bytes == null ? null : ClassInfoFactory.create(bytes);
	}
}
