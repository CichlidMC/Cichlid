package io.github.cichlidmc.cichlid.impl.metadata.component;

import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;
import org.jetbrains.annotations.Nullable;

public class DependencyImpl implements Dependency {
	private final String id;
	private final String name;
	private final VersionPredicate predicate;
	@Nullable
	private final String source;

	public DependencyImpl(String id, String name, VersionPredicate predicate, @Nullable String source) {
		this.id = id;
		this.name = name;
		this.predicate = predicate;
		this.source = assertNotDownload(source);
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public VersionPredicate predicate() {
		return this.predicate;
	}

	@Override
	@Nullable
	public String source() {
		return this.source;
	}

	public static Dependency parse(JsonObject json) throws JsonException {
		String id = json.get("id").asString().value();
		String name = json.get("name").asString().value();
		JsonString predicateJson = json.get("predicate").asString();
		String source = json.getOptional("source").map(value -> value.asString().value()).orElse(null);
		try {
			VersionPredicate parsed = VersionPredicate.parse(predicateJson.value());
			return new DependencyImpl(id, name, parsed, source);
		} catch (VersionPredicateSyntaxException e) {
			throw new JsonException(predicateJson, e.getMessage());
		}
	}

	@Nullable
	private static String assertNotDownload(@Nullable String source) {
		if (source == null)
			return null;

		if (source.trim().endsWith(".jar")) {
			throw new IllegalArgumentException("Dependency source appears to be a direct download link, which is forbidden: " + source);
		}

		return source;
	}
}
