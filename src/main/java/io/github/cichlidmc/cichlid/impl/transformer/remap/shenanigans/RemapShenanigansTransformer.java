package io.github.cichlidmc.cichlid.impl.transformer.remap.shenanigans;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.cichlid.impl.util.asm.MethodTarget;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Map;

public enum RemapShenanigansTransformer implements ClassFileTransformer {
	INSTANCE;

	public static final String CLASS_LOADER = "java/lang/ClassLoader";
	public static final Map<MethodTarget, Boolean> TARGETS = Utils.mapOf(
			// maps mapped -> unmapped so the class file can be found
			new MethodTarget("loadClass", Class.class, String.class), true,
			// maps unmapped -> mapped so the bytecode name matches the requested name
			new MethodTarget("defineClass", Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class), false,
			// maps unmapped -> mapped to find an existing remapped class
			new MethodTarget("findLoadedClass", Class.class, String.class), false
	);

	private static final CichlidLogger logger = CichlidLogger.get(RemapShenanigansTransformer.class);

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

		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		byte[] transformed = writer.toByteArray();

		try {
			Path output = CichlidPaths.CACHE.resolve("remap").resolve("ClassLoader.class");
			Files.deleteIfExists(output);
			Files.createDirectories(output.getParent());
			Files.write(output, transformed);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write transformed ClassLoader", e);
		}

		return transformed;
	}

	private void transform(ClassNode node) {
		TARGETS.forEach((target, reverse) -> {
			MethodNode method = target.find(node);
			if (method == null) {
				throw new RuntimeException("Failed to find " + target.name + " - you're likely using an unsupported JVM. Maybe update Cichlid?");
			}

			method.instructions.insert(RemapShenanigans.createInjection(reverse));

			// validation
			// method.maxStack += 20;
			// Analyzer<BasicValue> analyzer = new Analyzer<>(new SimpleVerifier());
			// try {
			// 	analyzer.analyze(node.name, method);
			// } catch (AnalyzerException e) {
			// 	throw new RuntimeException(e);
			// }
		});
	}
}
