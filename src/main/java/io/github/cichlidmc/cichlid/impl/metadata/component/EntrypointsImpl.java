package io.github.cichlidmc.cichlid.impl.metadata.component;

import io.github.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import io.github.cichlidmc.tinyjson.value.composite.JsonArray;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EntrypointsImpl implements Entrypoints {
	public static final Entrypoints EMPTY = new EntrypointsImpl(Collections.emptyMap());

	private final Map<String, List<String>> map;

	public EntrypointsImpl(Map<String, List<String>> map) {
		Map<String, List<String>> newMap = new HashMap<>(map.size());
		map.forEach((key, list) -> {
			if (!list.isEmpty()) {
				newMap.put(key, Utils.immutableCopy(list));
			}
		});
		this.map = Collections.unmodifiableMap(newMap);
	}

	@Override
	public List<String> get(String key) {
		return this.map.getOrDefault(key, Collections.emptyList());
	}

	@Override
	public boolean contains(String key) {
		return this.map.containsKey(key);
	}

	@Override
	public void forEach(BiConsumer<String, List<String>> consumer) {
		this.map.forEach(consumer);
	}

	public static Entrypoints parse(JsonObject json) throws JsonException {
		Map<String, List<String>> map = new HashMap<>();
		json.forEach((key, value) -> {
			if (value instanceof JsonString) {
				JsonString string = (JsonString) value;
				map.put(key, Collections.singletonList(string.value()));
			} else if (value instanceof JsonArray) {
				List<String> strings = ((JsonArray) value).stream()
						.map(JsonValue::asString)
						.map(JsonString::value)
						.collect(Collectors.toList());
				map.put(key, strings);
			} else {
				throw new JsonException(value, "Not a string or array");
			}
		});
		return new EntrypointsImpl(map);
	}
}
