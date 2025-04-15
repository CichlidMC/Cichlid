package io.github.cichlidmc.cichlid.impl.transformer.remap.shenanigans;

import io.github.cichlidmc.cichlid.impl.util.asm.MethodRef;
import net.neoforged.srgutils.IMappingFile;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;

public final class RemapShenanigans {
	public static final String BINARY_NAME = RemapShenanigans.class.getName();
	public static final MethodRef STRING_EQUALS = new MethodRef(String.class, "equals", MethodRef.InvokeType.VIRTUAL, boolean.class, Object.class);
	public static final MethodRef FIND_LOADED_CLASS = new MethodRef(ClassLoader.class, "findLoadedClass", MethodRef.InvokeType.VIRTUAL, Class.class, String.class);
	public static final MethodRef GET_METHOD = new MethodRef(Class.class, "getMethod", MethodRef.InvokeType.VIRTUAL, Method.class, String.class, Class[].class);
	public static final MethodRef INVOKE = new MethodRef(Method.class, "invoke", MethodRef.InvokeType.VIRTUAL, Object.class, Object.class, Object[].class);

	private static IMappingFile mappings;
	private static IMappingFile reversed;

	public static void apply(IMappingFile mappings, Instrumentation instrumentation) {
		if (RemapShenanigans.mappings != null) {
			throw new IllegalStateException("apply was called twice!");
		}

		RemapShenanigans.mappings = mappings;
		RemapShenanigans.reversed = mappings.reverse();

		try {
			// we need to make sure the reflection internals are fully set up, so we don't get a stack overflow in loadClass.
			// On temurin 8, a class is generated after 15 invocations. Let's go with 30 just to be safe.
			for (int i = 0; i < 30; i++) {
				RemapShenanigans.class.getMethod("remap", String.class).invoke(null, "dummy");
				RemapShenanigans.class.getMethod("remapReversed", String.class).invoke(null, "dummy");
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to pre-load critical reflection internals", e);
		}

		try {
			instrumentation.addTransformer(RemapShenanigansTransformer.INSTANCE, true);
			instrumentation.retransformClasses(ClassLoader.class);
		} catch (UnmodifiableClassException e) {
			throw new RuntimeException("Current JVM does not support retransforming ClassLoader", e);
		}
	}

	public static String remap(String name) {
		return remap(name, mappings);
	}

	public static String remapReversed(String name) {
		return remap(name, reversed);
	}

	private static String remap(String name, IMappingFile mappings) {
		if (name == null)
			return null;

		if (mappings == null) {
			throw new IllegalStateException("Mappings are missing!");
		}

		String internal = name.replace('.', '/');
		String remapped = mappings.remapClass(internal);
		if (remapped.equals(internal))
			return name;

		return remapped.replace('/', '.');
	}

	/*
	this injection creates approximately the following code:

	if (!"RemapShenanigans".equals(name)) {
		Class shenanigans = this.findLoadedClass("...");
		if (shenanigans != null) {
			name = shenanigans.getMethod("remap").invoke(null, [name]);
		}
	}

	reflection must be used, since class loaders cannot directly access the classes they load.
	 */
	static InsnList createInjection(boolean reverse) {
		InsnList list = new InsnList();

		// label for after outer if statement
		LabelNode outer = new LabelNode();

		// if (!"RemapShenanigans".equals(name))

		// push class name constant
		list.add(new LdcInsnNode(BINARY_NAME));
		// push name
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		// invoke equals
		list.add(STRING_EQUALS.toNode());
		// jump if false
		list.add(new JumpInsnNode(Opcodes.IFNE, outer));

		// this.findLoadedClass(...)

		// push this
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		// push name
		list.add(new LdcInsnNode(BINARY_NAME));
		// invoke findLoadedClass
		list.add(FIND_LOADED_CLASS.toNode());

		// label for after inner if statement
		LabelNode inner = new LabelNode();

		// if (class != null)

		list.add(new InsnNode(Opcodes.DUP));
		list.add(new JumpInsnNode(Opcodes.IFNULL, inner));

		// class.getMethod("remap")

		// push method name
		list.add(new LdcInsnNode(reverse ? "remapReversed" : "remap"));
		// push parameter array
		list.add(new InsnNode(Opcodes.ICONST_1));
		list.add(new TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(Class.class)));
		// insert String.class parameter
		list.add(new InsnNode(Opcodes.DUP));
		list.add(new InsnNode(Opcodes.ICONST_0));
		list.add(new LdcInsnNode(Type.getType(String.class)));
		list.add(new InsnNode(Opcodes.AASTORE));
		// invoke getMethod
		list.add(GET_METHOD.toNode());

		// method.invoke(null, [name]);

		// push null
		list.add(new InsnNode(Opcodes.ACONST_NULL));
		// push parameter array
		list.add(new InsnNode(Opcodes.ICONST_1));
		list.add(new TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(String.class)));
		// insert name parameter
		list.add(new InsnNode(Opcodes.DUP));
		list.add(new InsnNode(Opcodes.ICONST_0));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new InsnNode(Opcodes.AASTORE));
		// invoke
		list.add(INVOKE.toNode());
		// make sure we got a string
		list.add(new InsnNode(Opcodes.DUP));
		list.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(String.class)));
		// store result
		list.add(new VarInsnNode(Opcodes.ASTORE, 1));

		// end inner if

		list.add(inner);

		// there's still a class on the stack, pop it
		list.add(new InsnNode(Opcodes.POP));

		// end outer if

		list.add(outer);

		return list;
	}
}
