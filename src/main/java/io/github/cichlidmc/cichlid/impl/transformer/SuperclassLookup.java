package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.impl.remap.ReadingClassProvider;
import net.neoforged.art.api.ClassProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class SuperclassLookup {
	public static final String OBJECT = "java/lang/Object";

	@Nullable
	private final ClassLoader loader;
	private final Map<String, String> cache;

	public SuperclassLookup(@Nullable ClassLoader loader) {
		this.loader = loader;
		this.cache = Collections.synchronizedMap(new HashMap<>());
	}

	@Nullable
	public String getSuperclass(String name) {
		return this.cache.computeIfAbsent(name, this::compute);
	}

	/**
	 * Parallel to {@link Class#isAssignableFrom(Class)}. Does not account for interfaces.
	 */
	public boolean isSameOrSuper(String name, String other) {
		if (name.equals(other))
			return true;

		while (true) {
			String superclass = this.getSuperclass(other);
			if (superclass == null) {
				return false;
			} else if (name.equals(superclass)) {
				return true;
			} else {
				other = superclass;
			}
		}
	}

	@Nullable
	private String compute(String name) {
		ClassProvider.IClassInfo info = ReadingClassProvider.readClassInfo(this.loader, name);
		if (info == null) {
			// class wasn't found, assume Object
			return OBJECT;
		}

		// treat interfaces as subclasses of Object too
		if ((info.getAccess() & Opcodes.ACC_INTERFACE) != 0) {
			return OBJECT;
		}

		return info.getSuper();
	}
}
