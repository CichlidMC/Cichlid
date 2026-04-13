package fish.cichlidmc.cichlid.impl.metadata.component;

import fish.cichlidmc.cichlid.api.metadata.component.Entrypoints;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.JsonValue;
import fish.cichlidmc.tinyjson.value.composite.JsonArray;
import fish.cichlidmc.tinyjson.value.composite.JsonObject;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EntrypointsImpl implements Entrypoints {
	public static final Entrypoints EMPTY = new EntrypointsImpl(Map.of());

	private final Map<String, List<String>> map;

	public EntrypointsImpl(Map<String, List<String>> map) {
		Map<String, List<String>> newMap = new HashMap<>(map.size());
		map.forEach((key, list) -> {
			if (!list.isEmpty()) {
				newMap.put(key, List.copyOf(list));
			}
		});
		this.map = Collections.unmodifiableMap(newMap);
	}

	@Override
	public List<String> get(String key) {
		return this.map.getOrDefault(key, List.of());
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
