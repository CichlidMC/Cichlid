package fish.cichlidmc.cichlid.impl.metadata.component;

import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.cichlid.api.metadata.component.Dependency;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import fish.cichlidmc.cichlid.impl.metadata.component.condition.ConditionRegistry;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.composite.JsonObject;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class DependencyImpl implements Dependency {
	private final String id;
	private final String name;
	private final Predicate<ModVersion> predicate;
	@Nullable
	private final String source;
	private final Collection<Condition> conditions;

	public DependencyImpl(String id, String name, Predicate<ModVersion> predicate, @Nullable String source, Collection<Condition> conditions) {
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
	public Predicate<ModVersion> predicate() {
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
		String name = json.getOrThrow("name").asString().value();
		JsonString predicateJson = json.getOrThrow("predicate").asString();
		String source = json.getOptional("source").map(value -> value.asString().value()).orElse(null);
		Collection<Condition> conditions = ConditionRegistry.parse(json.get("conditions"));
		try {
			Predicate<ModVersion> parsed = ModVersion.parsePredicate(predicateJson.value());
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
