package io.github.cichlidmc.cichlid.impl.remap.signing;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public final class SigningShenanigans {
	public static final String OWNER = Type.getInternalName(SigningShenanigans.class);
	public static final String NAME = "discardComparison";
	public static final String DESC = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.BOOLEAN_TYPE);

	public static void apply(Instrumentation instrumentation) {
		instrumentation.addTransformer(SigningBypassTransformer.INSTANCE, true);
		try {
			instrumentation.retransformClasses(ClassLoader.class);
		} catch (UnmodifiableClassException e) {
			throw new RuntimeException("Current JVM does not support retransforming ClassLoader", e);
		}
	}

	static AbstractInsnNode createInjection() {
		return new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, NAME, DESC);
	}

	public static boolean discardComparison(boolean matches) {
		return true;
	}
}
