package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.impl.CichlidImpl;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.sushi.api.TransformerManager;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

public final class CichlidTransformer implements ClassFileTransformer {
	private static final CichlidLogger logger = CichlidLogger.get(CichlidTransformer.class);

	private static boolean stopped = false;

	private final AtomicInteger unnamedClassId = new AtomicInteger();

	private TransformerManager manager;

	public void init(TransformerManager manager) {
		this.manager = manager;
	}

	@Override
	public byte[] transform(ClassLoader loader, @Nullable String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		try {
			return this.doTransform(name, bytes);
		} catch (Throwable t) {
			String nameToReport = name != null ? name : "<unnamed class #" + this.unnamedClassId.getAndIncrement() + '>';
			return ClassPoisoner.poison(bytes, nameToReport, t);
		}
	}

	// name is null for generated classes like lambda infrastructure, docs do not mention this
	private byte[] doTransform(@Nullable String name, byte[] bytes) {
		if (stopped) {
			return null;
		}

		if (name != null && !CichlidImpl.isInitialized()) {
			// catch Minecraft classes loading too early
			if (name.startsWith("net/minecraft") || name.startsWith("com/mojang")) {
				return ClassPoisoner.poison(bytes, name, new Throwable("Tried to load a Minecraft class too early: " + name));
			}
		}

		if (name != null && name.startsWith("io/github/cichlidmc/cichlid/")) {
			// don't let mods transform Cichlid itself
			return null;
		}

		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean transformed = EnvironmentStripper.strip(node);
		if (this.manager != null) {
			transformed |= this.manager.transform(node, reader);
		}

		if (!transformed)
			return null;

		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}

	public static void emergencyStop() {
		stopped = true;
	}
}
