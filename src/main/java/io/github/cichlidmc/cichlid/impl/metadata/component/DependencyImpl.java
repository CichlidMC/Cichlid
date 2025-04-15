package io.github.cichlidmc.cichlid.impl.metadata.component;

import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.cichlid.impl.metadata.component.condition.ConditionRegistry;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DependencyImpl implements Dependency {
	private final String id;
	private final String name;
	private final VersionPredicate predicate;
	@Nullable
	private final String source;
	private final Collection<Condition> conditions;

	public DependencyImpl(String id, String name, VersionPredicate predicate, @Nullable String source, Collection<Condition> conditions) {
		this.id = id;
		this.name = name;
		this.predicate = predicate;
		this.source = assertNotDownload(source);
		this.conditions = conditions;
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

	@Override
	public Collection<Condition> conditions() {
		return this.conditions;
	}

	public static Dependency parse(String id, JsonObject json) throws JsonException {
		String name = json.get("name").asString().value();
		JsonString predicateJson = json.get("predicate").asString();
		String source = json.getOptional("source").map(value -> value.asString().value()).orElse(null);
		Collection<Condition> conditions = ConditionRegistry.parse(json.getNullable("conditions"));
		try {
			VersionPredicate parsed = VersionPredicate.parse(predicateJson.value());
			return new DependencyImpl(id, name, parsed, source, conditions);
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
