package io.github.cichlidmc.cichlid.impl.util;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Utils {
	public static <T> T make(Supplier<T> supplier) {
		return supplier.get();
	}

	public static <T> T getOrThrow(@Nullable T value, String message) {
		if (value == null) {
			throw new IllegalStateException(message);
		}

		return value;
	}

	public static <T> T nextOrNull(List<T> list, int i) {
		int next = i + 1;
		return next == list.size() ? null : list.get(next);
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

	public static <T> List<T> immutableCopy(List<T> original) {
		return Collections.unmodifiableList(new ArrayList<>(original));
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

	public static <T> Set<T> createIdentityHashSet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	public static String repeat(String s, int times) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < times; i++) {
			builder.append(s);
		}
		return builder.toString();
	}

	public static byte[] readAllBytes(InputStream stream) throws IOException {
		try (ByteArrayOutputStream collector = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];

			while (true) {
				int read = stream.read(buffer);
				if (read == -1) {
					return collector.toByteArray();
				} else {
					collector.write(buffer, 0, read);
				}
			}
		}
	}
}
