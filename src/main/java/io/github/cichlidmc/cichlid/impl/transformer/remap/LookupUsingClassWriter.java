package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.transformer.SuperclassLookup;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public final class LookupUsingClassWriter extends ClassWriter {
	private final SuperclassLookup lookup;

	public LookupUsingClassWriter(@Nullable ClassReader classReader, int flags, SuperclassLookup lookup) {
		super(classReader, flags);
		this.lookup = lookup;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		if (type1.equals(type2))
			return type1;

		if (this.lookup.isSameOrSuper(type1, type2)) {
			return type1;
		} else if (this.lookup.isSameOrSuper(type2, type1)) {
			return type2;
		} else {
			do {
				type1 = this.lookup.getSuperclass(type1);
				if (type1 == null) {
					return SuperclassLookup.OBJECT;
				}
			} while (!this.lookup.isSameOrSuper(type1, type2));
			return type1;
		}
	}
}
