package io.github.cichlidmc.cichlid.impl.transformer.remap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

/**
 * The synchronization in EnhancedRemapper.getClass needs to be removed. Consider the following situation:
 * <ul>
 *     <li>class A is loaded on the main thread</li>
 *     <li>class A references class B</li>
 *     <li>MClass for B is queried and not found</li>
 *     <li>IClassInfo for B is queried</li>
 *     <li>load of B is dispatched to a new thread Thread-1</li>
 *     <li>MClass for B is queried and not found again</li>
 *     <li>deadlock!</li>
 * </ul>
 * Discarding it should be fine. The MClass will just be calculated twice.
 */
public final class EnhancedRemapperTransformer {
	// this is properly relocated by Shadow
	public static final String CLASS = "net/neoforged/art/internal/EnhancedRemapper";

	public static final String OWNER = Type.getInternalName(EnhancedRemapperTransformer.class);
	public static final String DESC = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(String.class));

	public static byte[] run(byte[] bytes) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		MethodNode method = node.methods.stream()
				.filter(m -> m.name.equals("getClass"))
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("getClass was not found"));

		for (AbstractInsnNode instruction : method.instructions) {
			if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).name.equals("intern")) {
				method.instructions.set(instruction, new MethodInsnNode(
						Opcodes.INVOKESTATIC, OWNER, "getDummy", DESC
				));

				ClassWriter writer = new ClassWriter(0);
				node.accept(writer);
				return writer.toByteArray();
			}
		}

		throw new RuntimeException("Couldn't find String.intern() call");
	}

	public static Object getDummy(String s) {
		return new Dummy();
	}

	public static final class Dummy {
	}
}
