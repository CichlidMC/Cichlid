package io.github.cichlidmc.cichlid.api.mod.entrypoint;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.cichlidmc.cichlid.api.mod.CichlidLoader;
import io.github.cichlidmc.cichlid.api.Mod;

public class EntrypointHelper {
	/**
	 * Invoke the given entrypoint on all mods.
	 * <br>
	 * The entrypoint will be queried on all mods, and if present, the class will
	 * be created from a public no-arg constructor.
	 * It will be cast to the given class, and the given consumer will be invoked.
	 * <br>
	 * Each time the consumer is invoked, it will be wrapped in a try-catch, and any
	 * error will be re-thrown as a {@link EntrypointException} which may optionally be caught.
	 * This will stop invoking entrypoints early; if you want to always invoke all of them, use
	 * {@link #invokeSafe(Class, String, BiConsumer, Consumer)}.
	 * @param clazz the expected class of implementations
	 * @param key the key for the entrypoint
	 * @param consumer a consumer that will be invoked for every instance
	 * @throws EntrypointException in the case of any exception during the consumer, or a type mismatch
	 */
	public static <T> void invoke(Class<T> clazz, String key, BiConsumer<T, Mod> consumer) {
		invokeSafe(clazz, key, consumer, exception -> {
			throw exception;
		});
	}

	/**
	 * Identical to {@link #invoke(Class, String, BiConsumer)}, but errors during invocation
	 * are non-fatal. Instead of stopping and throwing the exception, the error consumer will be invoked.
	 * It's recommended to add logging here.
	 */
	public static <T> void invokeSafe(Class<T> clazz, String key, BiConsumer<T, Mod> consumer,
									  Consumer<EntrypointException> errorConsumer) {
		List<Mod> list = CichlidLoader.mods().stream().filter(mod -> mod.metadata().entrypoints().has(key)).toList();
		if (list.isEmpty())
			return;

		for (Mod mod : list) {
			List<String> classNames = mod.metadata().entrypoints().get(key);
			for (String className : classNames) {
				try {
					Class<?> implClass = Class.forName(className);
					Object impl = implClass.getConstructor().newInstance();
					T entrypoint = clazz.cast(impl);
					consumer.accept(entrypoint, mod);
				} catch (Throwable t) {
					errorConsumer.accept(new EntrypointException(mod, key, t));
				}
			}
		}
	}
}
