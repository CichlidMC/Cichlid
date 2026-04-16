package fish.cichlidmc.cichlid.impl.sushi;

import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.cichlid.impl.logging.CichlidLogger;
import fish.cichlidmc.sushi.api.transformer.infra.Operation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public final class BuiltInSushiHooks {
	private static final CichlidLogger logger = CichlidLogger.get(BuiltInSushiHooks.class);

	public static void onMain() {
		logger.info("Main hook called!");
	}

	public static Object[] addJarsWithInstrumentationInstead(List<URL> urls, Object[] dest, Operation<Object[]> original) {
		Instrumentation instrumentation = CichlidImpl.INSTRUMENTATION.get();

		try {
			for (URL url : urls) {
				File file = Paths.get(url.toURI()).toFile();
				try (JarFile jar = new JarFile(file)) {
					instrumentation.appendToSystemClassLoaderSearch(jar);
				}
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Failed to add jar to classpath", e);
		}

		return dest;
	}

	public static Class<?> useOriginalClassLoader(String name, boolean init, ClassLoader loader, Operation<Class<?>> original) {
		ClassLoader originalLoader = original.getClass().getClassLoader();
		return original.call(name, init, originalLoader);
	}

	public static void doNotSetCcl(Thread thread, ClassLoader loader, Operation<Void> original) {
		// intentionally empty
	}

	public static String modifyClientBrand(Operation<String> original) {
		return modifyBrand(original.call());
	}

	public static String modifyServerBrand(Object minecraftServer, Operation<String> original) {
		return modifyBrand(original.call(minecraftServer));
	}

	private static String modifyBrand(String original) {
		return original.equals("vanilla") ? CichlidImpl.BRAND : CichlidImpl.BRAND + " (+ " + original + ')';
	}
}
