package fish.cichlidmc.cichlid.impl.transformer;

import fish.cichlidmc.cichlid.api.CichlidPaths;
import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.cichlid.impl.logging.CichlidLogger;
import fish.cichlidmc.cichlid.impl.util.FileUtils;
import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFile.ClassHierarchyResolverOption;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public enum CichlidTransformer implements ClassFileTransformer {
	INSTANCE;

	public static final List<String> JAVA_PACKAGES = List.of("java/", "jdk/", "sun/", "javax/");
	public static final List<String> MINECRAFT_PACKAGES = List.of("net.minecraft", "com.mojang");
	public static final boolean EXPORT = Boolean.getBoolean(CichlidImpl.propertyName("transform.export"));
	public static final Path EXPORT_ROOT = CichlidPaths.CICHLID_ROOT.resolve("transform-export");

	private static final CichlidLogger logger = CichlidLogger.get(CichlidTransformer.class);

	private static final List<ClassFile.Option> classFileOptions = List.of(
			// we need a custom hierarchy resolver that reads class files instead of loading classes
			ClassHierarchyResolverOption.of(ClassHierarchyResolver.ofResourceParsing(CichlidTransformer::findClassFile).cached())
	);

	// the hierarchy resolver needs to know the current classloader
	private static final ScopedValue<Optional<ClassLoader>> currentClassLoader = ScopedValue.newInstance();

	// we need a ClassFile instance we can use before Sushi is initialized
	private static final ClassFile earlyClassFile = ClassFile.of(classFileOptions.toArray(ClassFile.Option[]::new));

	private static boolean stopped = false;

	@Nullable
	private static TransformerManager sushiManager;

	@Override
	public byte @Nullable [] transform(@Nullable ClassLoader loader, @Nullable String name, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
		// - don't transform unnamed classes, gets weird
		// - if transforming was emergency stopped, do nothing
		// - don't transform classes from Java itself, opens too many cans of worms
		// - don't let mods transform Cichlid itself for the sake of stability. If you're a disgruntled modder reading this line, sorry, but please open an issue or PR!
		if (name == null || stopped || isJavaClass(name) || name.startsWith("io/github/cichlidmc/cichlid/"))
			return null;

		Optional<ClassDesc> maybeDesc = parseDesc(name);
		if (maybeDesc.isEmpty()) {
			// invalid class name for some reason. nothing we can really do here
			logger.warn("Failed to parse class name into a desc: " + name);
			return null;
		}

		ClassDesc desc = maybeDesc.get();

		try {
			return transformSafe(loader, desc, bytes).orElse(null);
		} catch (Throwable t) {
			logger.error("Unhandled exception while transforming class " + name);
			logger.throwable(t);

			try {
				byte[] withCallback = injectClassLoadCallback(bytes);
				CichlidClassLoadCallbacks.registerException(loader, desc, t);
				maybeExport(desc, withCallback);
				return withCallback;
			} catch (Throwable t2) {
				logger.error("Failed to inject error-handling classload callback into class " + name);
				logger.throwable(t2);
				return null;
			}
		}
	}

	private static Optional<byte[]> transformSafe(@Nullable ClassLoader loader, ClassDesc name, byte[] bytes) {
		if (sushiManager == null) {
			// best-effort check for Minecraft classes loading too early
			if (isMinecraftClass(name)) {
				throw new RuntimeException("Tried to load a Minecraft class too early: " + ClassDescs.fullName(name));
			}

			return Optional.empty();
		}

		Optional<TransformResult> maybeResult = ScopedValue.where(currentClassLoader, Optional.ofNullable(loader)).call(
				() -> sushiManager.transform(bytes, name)
		);

		if (maybeResult.isEmpty()) {
			return Optional.empty();
		}

		TransformResult result = maybeResult.get();
		Requirements requirements = result.requirements();
		if (requirements.isEmpty()) {
			byte[] finalBytes = result.bytes();
			maybeExport(name, finalBytes);
			return Optional.of(finalBytes);
		}

		// there's requirements to check. we can't check them now, because we're in the middle of transforming a class.
		// we need to inject a callback into the head of class init to check them then.
		byte[] newBytes = injectClassLoadCallback(result.bytes());
		// only do this after transforming to make sure it didn't fail
		CichlidClassLoadCallbacks.registerRequirements(loader, name, requirements);

		maybeExport(name, newBytes);
		return Optional.of(newBytes);
	}

	private static byte[] injectClassLoadCallback(byte[] bytes) {
		ClassFile classFile = sushiManager == null ? earlyClassFile : sushiManager.classFile().get();
		ClassModel transformedModel = classFile.parse(bytes);
		return classFile.transformClass(transformedModel, new CichlidClassLoadCallbacks.Injector());
	}

	private static void maybeExport(ClassDesc desc, byte[] bytes) {
		if (!EXPORT)
			return;

		String name = ClassDescs.fullName(desc);
		Path file = EXPORT_ROOT.resolve(name.replace('.', '/') + ".class");
		Path parent = file.getParent();

		try {
			Files.createDirectories(parent);
			Files.write(file, bytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new RuntimeException("Failed to export transformed class " + name, e);
		}
	}

	public static void init(Instrumentation instrumentation) {
		instrumentation.addTransformer(INSTANCE);

		try {
			FileUtils.deleteRecursively(EXPORT_ROOT);
		} catch (IOException e) {
			throw new RuntimeException("Failed to clear export directory", e);
		}

		if (EXPORT) {
			logger.info("Transformed classes will be exported to " + EXPORT_ROOT);
		}
	}

	public static void initSushi(Consumer<TransformerManager.Builder> consumer) {
		if (sushiManager != null) {
			throw new IllegalStateException("Sushi has already been initialized");
		}

		TransformerManager.Builder builder = TransformerManager.builder();
		classFileOptions.forEach(builder::addClassFileOption);

		consumer.accept(builder);
		sushiManager = builder.build();

		int transformers = sushiManager.transformers().size();
		int phases = sushiManager.phases().size();
		logger.info("Sushi initialized with " + transformers + " transformer(s) across " + phases + " phase(s)");
	}

	public static void emergencyStop() {
		stopped = true;
	}

	private static boolean isJavaClass(String name) {
		for (String pkg : JAVA_PACKAGES) {
			if (name.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMinecraftClass(ClassDesc name) {
		for (String pkg : MINECRAFT_PACKAGES) {
			if (name.packageName().startsWith(pkg)) {
				return true;
			}
		}

		return false;
	}

	private static Optional<ClassDesc> parseDesc(String name) {
		try {
			return Optional.of(ClassDesc.ofInternalName(name));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	@Nullable
	private static InputStream findClassFile(ClassDesc desc) {
		ClassLoader loader = currentClassLoader.orElseThrow(
				() -> new IllegalStateException("Current ClassLoader is not set")
		).orElseGet(CichlidTransformer.class::getClassLoader);

		String path = ClassDescs.fullName(desc).replace('.', '/') + ".class";
		return loader.getResourceAsStream(path);
	}
}
