package io.github.cichlidmc.cichlid.impl.util.asm;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class MethodTarget {
	public final String name;
	public final String desc;

	public MethodTarget(String name, Class<?> returnType, Class<?>... params) {
		this.name = name;
		this.desc = MethodRef.createDesc(returnType, params);
	}

	@Nullable
	public MethodNode find(ClassNode clazz) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(this.name) && method.desc.equals(this.desc)) {
				return method;
			}
		}

		return null;
	}
}
