package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.api.transformer.CichlidTransformer;
import io.github.cichlidmc.cichlid.impl.CichlidImpl;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public enum CichlidTransformerManager implements ClassFileTransformer {
	INSTANCE;

	private static final CichlidLogger logger = CichlidLogger.get(CichlidTransformerManager.class);
	private static final List<CichlidTransformer> transformers = new ArrayList<>();

	private static boolean stopped = false;

	public static void registerTransformer(CichlidTransformer transformer) {
		transformers.add(transformer);
	}

	public static void emergencyStop() {
		stopped = true;
	}

	@Override
	public byte[] transform(ClassLoader loader, String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		if (stopped) {
			return null;
		}

		if (!CichlidImpl.isInitialized()) {
			// try to catch Minecraft classes loading too early
			if (name.startsWith("net/minecraft") || name.startsWith("com/mojang")) {
				// TODO: do something about this
			}
		}

		if (name.startsWith("io/github/cichlidmc/cichlid/")) {
			// don't let mods transform Cichlid itself
			return null;
		}

		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean transformed = false;
		for (CichlidTransformer transformer : transformers) {
			try {
				transformed |= transformer.transform(node);
			} catch (Throwable t) {
				logger.error("Error while transforming class " + name + ", thrown by " + transformer);
				logger.throwable(t);
				// TODO: shutdown when this happens.
				// need an API for shutdown hooks so the game can safely exit, and then display the GUI.
			}
		}

		if (!transformed)
			return null;

		ClassWriter writer = new ClassWriter(reader, 0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
