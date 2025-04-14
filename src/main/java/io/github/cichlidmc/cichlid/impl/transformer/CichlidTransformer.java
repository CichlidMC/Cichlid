package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.impl.CichlidImpl;
import io.github.cichlidmc.cichlid.impl.remap.ReadingClassProvider;
import io.github.cichlidmc.cichlid.impl.transformer.remap.EnhancedRemapperTransformer;
import io.github.cichlidmc.cichlid.impl.transformer.remap.LookupUsingClassWriter;
import io.github.cichlidmc.cichlid.impl.transformer.remap.RemappedClass;
import io.github.cichlidmc.cichlid.impl.transformer.remap.RuntimeMinecraftRemapper;
import io.github.cichlidmc.cichlid.impl.util.ClassLoaderResource;
import io.github.cichlidmc.sushi.api.TransformerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public final class CichlidTransformer implements ClassFileTransformer {
	public static final List<String> JAVA_PACKAGES = Arrays.asList("java/", "jdk/", "sun/", "javax/");

	private static boolean stopped = false;

	private final ClassLoaderResource<SuperclassLookup> superclassLookups;
	private final ClassLoaderResource<ReadingClassProvider> classProviders;

	@Nullable
	private RuntimeMinecraftRemapper remapper;
	private TransformerManager manager;

	private CichlidTransformer() {
		this.superclassLookups = new ClassLoaderResource<>(SuperclassLookup::new);
		this.classProviders = new ClassLoaderResource<>(ReadingClassProvider::new);
	}

	@Override
	public byte[] transform(@Nullable ClassLoader loader, @Nullable String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		// don't transform unnamed classes, gets weird
		if (name == null)
			return null;

		// System.out.println("transforming " + name + " on " + loader + " on " + Thread.currentThread().getName());

		try {
			return this.transformSafe(loader, name, bytes);
		} catch (Throwable t) {
			return ClassPoisoner.poison(name, bytes, t);
		}
	}

	private byte[] transformSafe(@Nullable ClassLoader loader, String name, byte[] bytes) {
		if (stopped) {
			return null;
		}

		// don't transform built-in Java classes, that opens too many cans of worms
		if (isJavaClass(name))
			return null;

		// special case, see javadoc
		if (name.equals(EnhancedRemapperTransformer.CLASS)) {
			return EnhancedRemapperTransformer.run(bytes);
		}

		// don't let mods transform Cichlid itself
		if (name.startsWith("io/github/cichlidmc/cichlid/")) {
			return null;
		}

		if (this.remapper != null) {
			ReadingClassProvider provider = this.classProviders.get(loader);
			RemappedClass remapped = this.remapper.remap(provider, name, bytes);
			if (remapped != null) {
				byte[] transformed = this.transformRemapped(loader, remapped.name, remapped.bytes);
				return transformed == null ? remapped.bytes : null;
			}
		}

		return this.transformRemapped(loader, name, bytes);
	}

	private byte[] transformRemapped(@Nullable ClassLoader loader, String name, byte[] bytes) {
		// best-effort check for Minecraft classes loading too early
		if (this.manager == null && (name.startsWith("net/minecraft") || name.startsWith("com/mojang"))) {
			throw new RuntimeException("Tried to load a Minecraft class too early: " + name);
		}

		ClassReader reader = new ClassReader(bytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean transformed = EnvironmentStripper.strip(node);
		if (this.manager != null) {
			transformed |= this.manager.transform(node, reader);
		}

		// if no transformations were applied, then skip writing
		if (!transformed)
			return null;

		SuperclassLookup lookup = this.superclassLookups.get(loader);
		ClassWriter writer = new LookupUsingClassWriter(reader, ClassWriter.COMPUTE_FRAMES, lookup);
		node.accept(writer);
		// System.out.println("transformed " + name + " on " + Thread.currentThread().getName());
		return writer.toByteArray();
	}

	@ApiStatus.Internal
	public void setTransformerManager(TransformerManager manager) {
		if (this.manager != null) {
			throw new IllegalStateException("TransformerManager is already set!");
		}

		this.manager = manager;
	}

	@ApiStatus.Internal
	public void setRemapper(@Nullable RuntimeMinecraftRemapper remapper) {
		if (this.remapper != null) {
			throw new IllegalStateException("Remapper is already set!");
		}

		System.out.println("remapper set");
		this.remapper = remapper;
	}

	public static void emergencyStop() {
		stopped = true;
	}

	public static boolean isJavaClass(String name) {
		for (String pkg : JAVA_PACKAGES) {
			if (name.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	public static CichlidTransformer setup(Instrumentation instrumentation) {
		// ClassLookup.createInfo(Temporal.class);
		// ClassLookup.createInfo(JsonValue.class);
		// ClassLookup.createInfo(TransformerManager.Builder.class);
		// ClassLookup.createInfo(LockSupport.class);
		// ClassLookup.createInfo(EnhancedRemapperTransformer.Dummy.class);
		CichlidTransformer transformer = new CichlidTransformer();
		instrumentation.addTransformer(transformer);

		try {
			// make sure this class is loaded early
			Class.forName("net.neoforged.art.internal.EnhancedRemapper", false, CichlidImpl.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return transformer;
	}
}
