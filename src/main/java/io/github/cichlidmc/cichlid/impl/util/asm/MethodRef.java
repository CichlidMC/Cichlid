package io.github.cichlidmc.cichlid.impl.util.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

public final class MethodRef {
	private final String owner;
	private final String name;
	private final String desc;
	private final InvokeType type;

	public MethodRef(Class<?> owner, String name, InvokeType type, Class<?> returnType, Class<?>... params) {
		this.owner = Type.getInternalName(owner);
		this.name = name;
		this.desc = createDesc(returnType, params);
		this.type = type;
	}

	public MethodInsnNode toNode() {
		return new MethodInsnNode(this.type.opcode, this.owner, this.name, this.desc);
	}

	static String createDesc(Class<?> returnType, Class<?>... params) {
		return Type.getMethodDescriptor(
				Type.getType(returnType),
				Arrays.stream(params).map(Type::getType).toArray(Type[]::new)
		);
	}

	public enum InvokeType {
		VIRTUAL(Opcodes.INVOKEVIRTUAL),
		STATIC(Opcodes.INVOKESTATIC),
		DYNAMIC(Opcodes.INVOKEDYNAMIC),
		SPECIAL(Opcodes.INVOKESPECIAL),
		INTERFACE(Opcodes.INVOKEINTERFACE);

		public final int opcode;

		InvokeType(int opcode) {
			this.opcode = opcode;
		}
	}
}
