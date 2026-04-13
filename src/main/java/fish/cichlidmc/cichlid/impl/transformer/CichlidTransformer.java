package fish.cichlidmc.cichlid.impl.transformer;

import fish.cichlidmc.cichlid.impl.logging.CichlidLogger;
import fish.cichlidmc.sushi.api.TransformResult;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFile.ClassHierarchyResolverOption;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public enum CichlidTransformer implements ClassFileTransformer {
	INSTANCE;

	public static final List<String> JAVA_PACKAGES = List.of("java/", "jdk/", "sun/", "javax/");
	public static final List<String> MINECRAFT_PACKAGES = List.of("net/minecraft/", "com/mojang/");

	private static final CichlidLogger logger = CichlidLogger.get(CichlidTransformer.class);
	private static final ScopedValue<Optional<ClassLoader>> currentClassLoader = ScopedValue.newInstance();
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
			return this.transformSafe(loader, desc, bytes).orElse(null);
		} catch (Throwable t) {
			CichlidClassLoadCallbacks.registerException(loader, desc, t);
			logger.error("Unhandled exception while transforming class " + desc);
			logger.throwable(t);
			return null;
		}
	}

	private Optional<byte[]> transformSafe(@Nullable ClassLoader loader, ClassDesc name, byte[] bytes) {
		if (sushiManager == null) {
			// best-effort check for Minecraft classes loading too early
			if (isMinecraftClass(name)) {
				throw new RuntimeException("Tried to load a Minecraft class too early: " + name);
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
			return Optional.of(result.bytes());
		}

		// there's requirements to check. we can't check them now, because we're in the middle of transforming a class.
		// we need to inject a callback into the head of class init to check them then.
		ClassFile classFile = sushiManager.classFile().get();
		ClassModel transformedModel = classFile.parse(result.bytes());
		byte[] newBytes = classFile.transformClass(transformedModel, new CichlidClassLoadCallbacks.Injector());

		// only do this after transforming to make sure it didn't fail
		CichlidClassLoadCallbacks.registerRequirements(loader, name, requirements);

		return Optional.of(newBytes);
	}

	public static void initSushi(Consumer<TransformerManager.Builder> consumer) {
		if (sushiManager != null) {
			throw new IllegalStateException("Sushi has already been initialized");
		}

		TransformerManager.Builder builder = TransformerManager.builder();
		builder.addClassFileOption(ClassHierarchyResolverOption.of(createHierarchyResolver()));

		consumer.accept(builder);
		sushiManager = builder.build();
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

	private static ClassHierarchyResolver createHierarchyResolver() {
		return ClassHierarchyResolver.ofResourceParsing(desc -> {
			ClassLoader loader = currentClassLoader.orElseThrow(
					() -> new IllegalStateException("Current ClassLoader is not set")
			).orElseGet(CichlidTransformer.class::getClassLoader);

			String path = ClassDescs.fullName(desc).replace('.', '/') + ".class";
			return loader.getResourceAsStream(path);
		}).cached();
	}
}
