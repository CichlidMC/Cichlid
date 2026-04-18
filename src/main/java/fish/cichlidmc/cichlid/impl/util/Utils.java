package fish.cichlidmc.cichlid.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {
	public static <T> T make(Supplier<T> supplier) {
		return supplier.get();
	}

	@SafeVarargs
	public static <T> List<T> listOf(T... values) {
		// just an alias, but make the intention clear
		return mutableListOf(values);
	}

	@SafeVarargs
	public static <T> List<T> mutableListOf(T... values) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, values);
		return list;
	}

	public static <K, V> Map<K, V> mapOf(K k1, V v1, Object... more) {
		if (more.length % 2 != 0) {
			throw new IllegalArgumentException("Odd number of arguments: " + Arrays.toString(more));
		}

		Class<?> kClass = k1.getClass();
		Class<?> vClass = v1.getClass();
		Map<K, V> map = new HashMap<>();
		map.put(k1, v1);

		if (more.length == 0)
			return map;

		for (int i = 0; i < more.length; i += 2) {
			Object k = more[i];
			Object v = more[i + 1];
			if (!kClass.isInstance(k)) {
				throw new IllegalArgumentException("Incorrect type: " + k + " is not " + kClass);
			} else if (!vClass.isInstance(v)) {
				throw new IllegalArgumentException("Incorrect type: " + v + " is not " + vClass);
			} else {
				//noinspection unchecked
				map.put((K) k, (V) v);
			}
		}

		return map;
	}

	public static String getStackTrace(Throwable t) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(data);
		t.printStackTrace(stream);
		return data.toString();
	}

	public static String repeat(String s, int times) {
		StringBuilder builder = new StringBuilder();
		builder.repeat(s, Math.max(0, times));
		return builder.toString();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void ensureLoaded(Class<?> clazz) {
		clazz.getDeclaredMethods();
	}

	/// Convert the given [URL] into a [URI].
	/// @throws IllegalArgumentException if the conversion fails
	public static URI toUri(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URI", e);
		}
	}
}
