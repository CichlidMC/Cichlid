package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.transformer.callback.LoadCallbackManager;
import io.github.cichlidmc.cichlid.impl.transformer.lookup.ClassLookup;
import io.github.cichlidmc.cichlid.impl.transformer.lookup.ClassLookupStorage;
import io.github.cichlidmc.cichlid.impl.transformer.lookup.ClassLookups;
import io.github.cichlidmc.cichlid.impl.transformer.remap.EnhancedRemapperTransformer;
import io.github.cichlidmc.cichlid.impl.transformer.remap.LookupUsingClassWriter;
import io.github.cichlidmc.cichlid.impl.transformer.remap.MinecraftRemapper;
import io.github.cichlidmc.cichlid.impl.transformer.remap.RemappedClass;
import io.github.cichlidmc.sushi.api.TransformerManager;
import io.github.cichlidmc.tinyjson.value.JsonValue;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public final class CichlidTransformer implements ClassFileTransformer {
	public static final List<String> JAVA_PACKAGES = Arrays.asList("java/", "jdk/", "sun/", "javax/");

	private static final CichlidLogger logger = CichlidLogger.get(CichlidTransformer.class);

	private static boolean stopped = false;

	private final Executor subLoadExecutor;
	private final ClassLookupStorage lookups;

	private final LoadCallbackManager loadCallbacks;

	private TransformerManager manager;
	@Nullable
	private MinecraftRemapper remapper;

	private CichlidTransformer() {
		AtomicInteger h = new AtomicInteger();
		this.subLoadExecutor = Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setName("Cichlid-Sub-Load-" + h.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		});

		this.lookups = new ClassLookupStorage(this);
		this.loadCallbacks = new LoadCallbackManager();
	}

	public CompletableFuture<Class<?>> requestLoad(String name, ClassLoader loader) {
		// try {
			// CompletableFuture<LoadResult> resultFuture = this.loadCallbacks.register(name, loader);
			return CompletableFuture.supplyAsync(() -> load(name, loader), this.subLoadExecutor);
			// CompletableFuture.anyOf(resultFuture, loadFuture).get();
			// either:
			// - resultFuture is populated: class was loaded and transformed
			// - loadFuture is populated: class was not found
			// if (resultFuture.isDone()) {
			// 	LoadResult result = resultFuture.get();
			// 	result.receivedFuture.complete(null);
			// 	return result.bytes;
			// } else {
			// 	return null;
			// }
		// } catch (ExecutionException | InterruptedException e) {
		// 	throw new RuntimeException(e);
		// }
	}

	@Override
	public byte[] transform(@Nullable ClassLoader loader, @Nullable String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		// don't transform unnamed classes, gets weird
		if (name == null)
			return null;

		System.out.println("transforming " + name + " on " + loader + " on " + Thread.currentThread().getName());
		ClassLookups lookups = this.lookups.get(loader);

		try {
			lookups.preRemap.put(bytes);
			byte[] transformed = this.transformSafe(loader, name, bytes);
			if (transformed == null) {
				// if !null, postTransform was already handled
				lookups.postTransform.put(bytes);
			}
			return transformed;
		} catch (Throwable t) {
			try {
				return this.poison(loader, name, bytes, t);
			} catch (Throwable uhOh) {
				logger.error("Error while poisoning class " + name + "! Transformations will be silently discarded!");
				logger.throwable(uhOh);
				t.addSuppressed(uhOh);
				throw t;
			}
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
			ClassLookup lookup = this.lookups.get(loader).preRemap;
			RemappedClass remapped = this.remapper.remap(lookup, name, bytes);
			if (remapped != null) {
				return this.transformRemapped(loader, remapped.name, remapped.bytes);
			}
		}

		return this.transformRemapped(loader, name, bytes);
	}

	private byte[] transformRemapped(@Nullable ClassLoader loader, String name, byte[] bytes) {
		// best-effort check for Minecraft classes loading too early
		if (this.manager == null && (name.startsWith("net/minecraft") || name.startsWith("com/mojang"))) {
			return this.poison(loader, name, bytes, new Throwable("Tried to load a Minecraft class too early: " + name));
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

		ClassLookup lookup = this.lookups.get(loader).postTransform;
		lookup.put(bytes);
		ClassWriter writer = new LookupUsingClassWriter(reader, ClassWriter.COMPUTE_FRAMES, lookup);
		node.accept(writer);
		System.out.println("transformed " + name + " on " + Thread.currentThread().getName());
		return writer.toByteArray();
	}

	private byte[] poison(@Nullable ClassLoader loader, String name, byte[] bytes, Throwable error) {
		byte[] poisoned = ClassPoisoner.poison(name, bytes, error);
		this.lookups.get(loader).postTransform.put(poisoned);
		return poisoned;
	}

	@ApiStatus.Internal
	public void setTransformerManager(TransformerManager manager) {
		if (this.manager != null) {
			throw new IllegalStateException("TransformerManager is already set!");
		}

		this.manager = manager;
	}

	@ApiStatus.Internal
	public void setRemapper(@Nullable MinecraftRemapper remapper) {
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
		ClassLookup.createInfo(JsonValue.class);
		ClassLookup.createInfo(TransformerManager.Builder.class);
		ClassLookup.createInfo(LockSupport.class);
		ClassLookup.createInfo(EnhancedRemapperTransformer.Dummy.class);
		CichlidTransformer transformer = new CichlidTransformer();
		instrumentation.addTransformer(transformer);
		return transformer;
	}

	private static Class<?> load(String name, ClassLoader loader) {
		try {
			System.out.println("sub-loading " + name + " on " + loader + " on " + Thread.currentThread().getName());
			Class<?> clazz = Class.forName(name.replace('/', '.'), false, loader);
			System.out.println("Successfully finished sub-load of " + name + " on " + loader + " on " + Thread.currentThread().getName());
			return clazz;
		} catch (ClassNotFoundException ignored) {
			System.out.println("Failed sub-load of " + name + " on " + loader + " on " + Thread.currentThread().getName());
			return null;
		}
	}
}
