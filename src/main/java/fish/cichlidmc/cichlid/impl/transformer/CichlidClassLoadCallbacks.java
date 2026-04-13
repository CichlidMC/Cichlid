package fish.cichlidmc.cichlid.impl.transformer;

import fish.cichlidmc.cichlid.impl.util.ClassLoaderResource;
import fish.cichlidmc.fishflakes.api.value.Either;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassElement;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodElement;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessFlag;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Calls to this class are injected into the static init of most transformed classes.
/// This allows Cichlid to perform post-transform validation safely.
public final class CichlidClassLoadCallbacks {
	private static final ClassLoaderResource<CichlidClassLoadCallbacks> callbacks = new ClassLoaderResource<>(CichlidClassLoadCallbacks::new);

	private final Map<ClassDesc, Either<Throwable, Requirements>> info = Collections.synchronizedMap(new HashMap<>());

	private void onLoad(Class<?> clazz, MethodHandles.Lookup lookup) {
		ClassDesc desc = ClassDescs.of(clazz);
		switch (this.info.get(desc)) {
			case null -> throw new RuntimeException("A load callback was injected, but no info was found: " + desc);
			case Either.Left(Throwable throwable) -> this.handleThrowable(desc, throwable);
			case Either.Right(Requirements requirements) -> this.handleRequirements(lookup, requirements);
		}
	}

	private void handleThrowable(ClassDesc desc, Throwable throwable) {
		throw new TransformException("Exception while transforming class " + desc, throwable);
	}

	private void handleRequirements(MethodHandles.Lookup lookup, Requirements requirements) {
		RequirementInterpreters interpreters = RequirementInterpreters.forRuntime(lookup);
		List<Requirements.Problem> problems = requirements.check(interpreters);
		if (problems.isEmpty())
			return;

		TransformException exception = new TransformException("One or more requirements were unmet");
		problems.forEach(problem -> exception.addSuppressed(problem.exception()));
		throw exception;
	}

	// calls to this are injected into transformed classes
	@SuppressWarnings("unused")
	public static void invoke(MethodHandles.Lookup lookup) {
		Class<?> clazz = lookup.lookupClass();
		callbacks.get(clazz.getClassLoader()).onLoad(clazz, lookup);
	}

	static void registerException(@Nullable ClassLoader loader, ClassDesc desc, Throwable exception) {
		register(loader, desc, Either.left(exception));
	}

	static void registerRequirements(@Nullable ClassLoader loader, ClassDesc desc, Requirements requirements) {
		register(loader, desc, Either.right(requirements));
	}

	private static void register(@Nullable ClassLoader loader, ClassDesc desc, Either<Throwable, Requirements> either) {
		CichlidClassLoadCallbacks callbacks = CichlidClassLoadCallbacks.callbacks.get(loader);
		if (callbacks.info.containsKey(desc)) {
			throw new IllegalStateException("Duplicate info registration for class " + desc);
		}

		callbacks.info.put(desc, either);
	}

	public static final class Injector implements ClassTransform {
		private static final int staticInitFlags = AccessFlag.STATIC.mask();

		private boolean foundStaticInit;

		@Override
		public void accept(ClassBuilder builder, ClassElement element) {
			if (element instanceof MethodModel method && method.methodName().equalsString(ConstantDescs.CLASS_INIT_NAME)) {
				this.foundStaticInit = true;
				builder.transformMethod(method, this::transformMethod);
				return;
			}

			builder.with(element);
		}

		@Override
		public void atEnd(ClassBuilder builder) {
			if (this.foundStaticInit)
				return;

			builder.withMethod(
					ConstantDescs.CLASS_INIT_NAME, ConstantDescs.MTD_void, staticInitFlags,
					method -> method.withCode(code -> {
						CodeInjector.INSTANCE.atStart(code);
						code.return_();
					})
			);
		}

		private void transformMethod(MethodBuilder builder, MethodElement element) {
			if (element instanceof CodeModel code) {
				builder.transformCode(code, CodeInjector.INSTANCE);
			} else {
				builder.with(element);
			}
		}

		private enum CodeInjector implements CodeTransform {
			INSTANCE;

			/// [MethodHandles#lookup()]
			private static final DirectMethodHandleDesc methodHandlesLookup = MethodHandleDesc.ofMethod(
					DirectMethodHandleDesc.Kind.STATIC,
					ConstantDescs.CD_MethodHandles,
					"lookup",
					MethodTypeDesc.of(ConstantDescs.CD_MethodHandles_Lookup)
			);

			private static final DirectMethodHandleDesc invokeCallbacks = MethodHandleDesc.ofMethod(
					DirectMethodHandleDesc.Kind.STATIC,
					ClassDescs.of(CichlidClassLoadCallbacks.class),
					"invoke",
					MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_MethodHandles_Lookup)
			);

			@Override
			public void atStart(CodeBuilder builder) {
				Instructions.invokeMethod(builder, methodHandlesLookup);
				Instructions.invokeMethod(builder, invokeCallbacks);
			}

			@Override
			public void accept(CodeBuilder builder, CodeElement element) {
				builder.with(element);
			}
		}
	}
}
