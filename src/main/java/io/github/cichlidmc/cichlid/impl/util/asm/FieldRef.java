package io.github.cichlidmc.cichlid.impl.util.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

public final class FieldRef {
	private final String owner;
	private final String name;
	private final String desc;

	public FieldRef(Class<?> owner, String name, Class<?> type) {
		this.owner = Type.getInternalName(owner);
		this.name = name;
		this.desc = Type.getDescriptor(type);
	}

	public FieldInsnNode toNode(Operation operation) {
		return new FieldInsnNode(operation.opcode, this.owner, this.name, this.desc);
	}

	public enum Operation {
		GET(Opcodes.GETFIELD),
		SET(Opcodes.PUTFIELD),
		GET_STATIC(Opcodes.GETSTATIC),
		SET_STATIC(Opcodes.PUTSTATIC);

		public final int opcode;

		Operation(int opcode) {
			this.opcode = opcode;
		}
	}
}
