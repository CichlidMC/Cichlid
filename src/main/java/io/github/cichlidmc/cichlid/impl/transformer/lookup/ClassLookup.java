package io.github.cichlidmc.cichlid.impl.transformer.lookup;

import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import net.neoforged.art.api.ClassProvider;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class serves two purposes.
 * <ol>
 *     <li>
 *         {@link ClassWriter}, by default, calls {@link Class#forName(String)} when computing frames to find the
 *         lowest common superclass of two types. This class provides access to that information without calling forName.
 *     </li>
 *     <li>
 *         ART requires access to the remapped class and any classes it references. The built-in classpath finder
 *         somehow causes classes to get defined twice.
 *     </li>
 * </ol>
 */
public final class ClassLookup implements ClassProvider {
	public static final String OBJECT = "java/lang/Object";

	private static final CichlidLogger logger = CichlidLogger.get(ClassLookup.class);

	private final CichlidTransformer transformer;
	@Nullable
	private final ClassLoader loader;
	private final Map<String, IClassInfo> info;
	private final Map<String, List<CompletableFuture<Class<?>>>> awaitingClasses;
	private final Map<String, Object> classLocks;
	private final boolean pre;

	public ClassLookup(CichlidTransformer transformer, @Nullable ClassLoader loader, boolean pre) {
		this.transformer = transformer;
		this.loader = loader;
		this.pre = pre;
		// ConcurrentHashMap was deadlocking
		this.info = Collections.synchronizedMap(new HashMap<>());
		this.awaitingClasses = Collections.synchronizedMap(new HashMap<>());
		this.classLocks = Collections.synchronizedMap(new HashMap<>());

		this.info.put("java/lang/Enum", createInfo(Enum.class));
	}

	@Override
	public Optional<? extends IClassInfo> getClass(String name) {
		return Optional.ofNullable(this.getInfo(name));
	}

	@Nullable
	public IClassInfo getInfo(String name) {
		if (name == null)
			return null;

		// don't use computeIfAbsent here - we need the map to be mutable off-thread
		if (this.info.containsKey(name))
			return this.info.get(name);

		Object lock = this.classLocks.computeIfAbsent(name, $ -> new Object());
		synchronized (lock) {
			// check again, might've been computed on another thread
			if (this.info.containsKey(name))
				return this.info.get(name);

			IClassInfo computed = this.compute(name);
			this.info.put(name, computed);
			return computed;
		}
	}

	/**
	 * Parallel to {@link Class#isAssignableFrom(Class)}. Does not account for interfaces.
	 */
	public boolean isSameOrSuper(IClassInfo info, IClassInfo other) {
		String name = info.getName();
		if (name.equals(other.getName()))
			return true;

		while (true) {
			IClassInfo superInfo = this.getInfo(other.getSuper());
			if (superInfo == null) {
				return false;
			} else if (name.equals(superInfo.getName())) {
				return true;
			} else {
				other = superInfo;
			}
		}
	}

	public void put(byte[] bytes) {
		IClassInfo info = createInfo(bytes);
		String name = info.getName();
		System.out.println("Added " + name + " to " + this.loader + " on " + Thread.currentThread().getName() + " on " + this.pre);
		this.info.put(name, info);
		List<CompletableFuture<Class<?>>> futures = this.awaitingClasses.remove(name);
		if (futures != null) {
			futures.forEach(future -> future.cancel(true));
		}
	}

	@Nullable
	private IClassInfo compute(String name) {
		System.out.println("requesting " + name + " on " + this.loader + " on " + Thread.currentThread().getName() + " on " + this.pre);
		// load the class off-thread, since Java does not send loaded classes through agents if a transform is in-progress
		CompletableFuture<Class<?>> future = this.transformer.requestLoad(name, this.loader);
		// record that the class is being waited on, so it can be interrupted by put()
		this.awaitingClasses.computeIfAbsent(name, $ -> Collections.synchronizedList(new ArrayList<>())).add(future);

		try {
			Class<?> clazz = future.get();
			if (clazz == null)
				return null;

			// class has been loaded. Unfortunately, it's not as simple as creating an info instance from it,
			// since calling various reflection methods will cause it to initialize. Now we need to read the bytes.
			URL url = this.getResource(name + ".class");
			if (url == null) {
				logger.error("Got class " + name + ", but no bytes were found!");
				return null;
			}

			try (InputStream stream = url.openStream()) {
				byte[] bytes = Utils.readAllBytes(stream);
				return createInfo(bytes);
			}
		} catch (CancellationException cancellation) {
			// future was cancelled because the info was manually put() off-thread.
			if (this.info.containsKey(name)) {
				return this.info.get(name);
			} else {
				throw new IllegalStateException("Future was cancelled, but info is still missing?", cancellation);
			}
		} catch (IOException | InterruptedException | ExecutionException e) {
			// uh oh
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
	}

	@Nullable
	private Class<?> load(String name) {
		try {
			return Class.forName(name.replace('/', '.'), false, this.loader);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}

	private URL getResource(String name) {
		ClassLoader loader = this.loader == null ? this.getClass().getClassLoader() : this.loader;
		return loader.getResource(name);
	}

	public static IClassInfo createInfo(@Nullable Class<?> clazz) {
		if (clazz == null)
			return null;

		try {
			Class<?> infoClass = Class.forName("net.neoforged.art.internal.ClassProviderImpl$ClassInfo");
			Constructor<?> constructor = infoClass.getDeclaredConstructor(Class.class);
			constructor.setAccessible(true);
			return (IClassInfo) constructor.newInstance(clazz);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static IClassInfo createInfo(byte[] bytes) {
		try {
			Class<?> infoClass = Class.forName("net.neoforged.art.internal.ClassProviderImpl$ClassInfo");
			Constructor<?> constructor = infoClass.getDeclaredConstructor(byte[].class);
			constructor.setAccessible(true);
			//noinspection RedundantCast
			return (IClassInfo) constructor.newInstance((Object) bytes);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
