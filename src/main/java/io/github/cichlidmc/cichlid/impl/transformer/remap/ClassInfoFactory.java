package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.util.Utils;
import net.neoforged.art.api.ClassProvider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

public final class ClassInfoFactory {
	private static final MethodHandle constructor = Utils.make(() -> {
		try {
			Class<?> clazz = Class.forName("net.neoforged.art.internal.ClassProviderImpl$ClassInfo");
			Constructor<?> constructor = clazz.getDeclaredConstructor(byte[].class);
			constructor.setAccessible(true);
			return MethodHandles.lookup().unreflectConstructor(constructor);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	});

	public static ClassProvider.IClassInfo create(byte[] bytes) {
		try {
			return (ClassProvider.IClassInfo) constructor.invoke(bytes);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
