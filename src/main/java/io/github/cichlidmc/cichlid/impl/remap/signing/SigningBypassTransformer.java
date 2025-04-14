package io.github.cichlidmc.cichlid.impl.remap.signing;

import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;

public enum SigningBypassTransformer implements ClassFileTransformer {
	INSTANCE;

	public static final String CLASS_LOADER = "java/lang/ClassLoader";
	public static final String DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(CodeSource.class));

	private static final CichlidLogger logger = CichlidLogger.get(SigningBypassTransformer.class);

	@Override
	public byte[] transform(@Nullable ClassLoader loader, @Nullable String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (name == null || !name.equals(CLASS_LOADER))
			return null;

		try {
			return this.doTransform(bytes);
		} catch (Throwable t) {
			logger.error("Failed to transform ClassLoader, the game is probably about to crash");
			logger.throwable(t);
			return null;
		}
	}

	private byte[] doTransform(byte[] bytes) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		this.transform(node);

		ClassWriter writer = new ClassWriter(reader, 0);
		node.accept(writer);
		return writer.toByteArray();
	}

	private void transform(ClassNode node) {
		List<MethodNode> methods = node.methods.stream()
				.filter(method -> method.name.equals("checkCerts") && method.desc.equals(DESC))
				.collect(Collectors.toList());

		if (methods.isEmpty()) {
			throw fail("Failed to find checkCerts");
		} else if (methods.size() != 1) {
			throw fail("Multiple checkCerts found");
		}

		MethodNode method = methods.get(0);
		for (AbstractInsnNode instruction : method.instructions) {
			if (instruction instanceof MethodInsnNode && ((MethodInsnNode) instruction).name.equals("compareCerts")) {
				method.instructions.insert(instruction, SigningShenanigans.createInjection());
				return;
			}
		}

		throw fail("Failed to find compareCerts in checkCerts");
	}

	private static RuntimeException fail(String reason) {
		return new RuntimeException(reason + " - you're likely using an unsupported JVM. Maybe update Cichlid?");
	}
}
