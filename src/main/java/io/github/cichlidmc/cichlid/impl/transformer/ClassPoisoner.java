package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * So the JVM *really* insists on loading classes given to transformers.
 * Exceptions are silently ignored, and even replacing the class with an empty byte array doesn't crash it.
 * Solution: poison the class by injecting an exception into static init.
 */
public final class ClassPoisoner {
	private static final CichlidLogger logger = CichlidLogger.get(ClassPoisoner.class);
	private static final Map<String, Throwable> errors = Collections.synchronizedMap(new HashMap<>());

	// for injection
	public static final String owner = Type.getInternalName(ClassPoisoner.class);
	public static final String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class));

	public static byte[] poison(String name, byte[] bytes, Throwable t) {
		logger.error("Something went wrong while transforming class " + name + "; injecting poison.");
		logger.throwable(t);

		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		poison(node, name);

		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		byte[] newBytes = writer.toByteArray();

		errors.put(name, t);

		return newBytes;
	}

	private static void poison(ClassNode node, String name) {
		MethodNode method = getOrCreateStaticInit(node);
		InsnList toAdd = new InsnList();
		toAdd.add(new LdcInsnNode(name));
		toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "kill", desc));
		method.instructions.insert(toAdd);
	}

	private static MethodNode getOrCreateStaticInit(ClassNode node) {
		for (MethodNode method : node.methods) {
			if (method.name.equals("<clinit>")) {
				return method;
			}
		}

		MethodNode method = new MethodNode();
		method.access = Opcodes.ACC_STATIC;
		method.name = "<clinit>";
		method.desc = "()V";

		method.instructions.insert(new InsnNode(Opcodes.RETURN));

		node.methods.add(method);
		return method;
	}

	// called by poisoned classes
	public static void kill(String className) {
		Throwable error = errors.get(className);
		if (error == null) {
			throw new RuntimeException("Class was poisoned, but no reason was found? Name: " + className);
		} else {
			throw new RuntimeException("An error occurred while transforming class " + className, error);
		}
	}
}
