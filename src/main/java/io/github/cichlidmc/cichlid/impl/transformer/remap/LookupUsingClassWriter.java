package io.github.cichlidmc.cichlid.impl.transformer.remap;

import io.github.cichlidmc.cichlid.impl.transformer.lookup.ClassLookup;
import net.neoforged.art.api.ClassProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public final class LookupUsingClassWriter extends ClassWriter {
	private final ClassLookup lookup;

	public LookupUsingClassWriter(@Nullable ClassReader classReader, int flags, ClassLookup lookup) {
		super(classReader, flags);
		this.lookup = lookup;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		if (type1.equals(type2))
			return type1;

		ClassProvider.IClassInfo info1 = this.lookup.getInfo(type1);
		ClassProvider.IClassInfo info2 = this.lookup.getInfo(type2);
		if (info1 == null || info2 == null)
			return ClassLookup.OBJECT;

		if (this.lookup.isSameOrSuper(info1, info2)) {
			return type1;
		} else if (this.lookup.isSameOrSuper(info2, info1)) {
			return type2;
		} else if (isInterface(info1) || isInterface(info2)) {
			return ClassLookup.OBJECT;
		} else {
			do {
				info1 = this.lookup.getInfo(info1.getSuper());
				if (info1 == null) {
					return ClassLookup.OBJECT;
				}

			} while (!this.lookup.isSameOrSuper(info1, info2));
			return type1;
		}
	}

	private static boolean isInterface(ClassProvider.IClassInfo info) {
		return (info.getAccess() & Opcodes.ACC_INTERFACE) != 0;
	}
}
