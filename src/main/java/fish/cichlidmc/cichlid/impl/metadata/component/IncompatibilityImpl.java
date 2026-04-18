package fish.cichlidmc.cichlid.impl.metadata.component;

import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import fish.cichlidmc.cichlid.api.version.ModVersion;
import fish.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import fish.cichlidmc.cichlid.impl.metadata.component.condition.ConditionRegistry;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.composite.JsonObject;
import fish.cichlidmc.tinyjson.value.primitive.JsonString;

import java.util.Collection;
import java.util.function.Predicate;

public class IncompatibilityImpl implements Incompatibility {
	private final String id;
	private final Predicate<ModVersion> predicate;
	private final String reason;
	private final Collection<Condition> conditions;

	public IncompatibilityImpl(String id, Predicate<ModVersion> predicate, String reason, Collection<Condition> conditions) {
		this.id = id;
		this.predicate = predicate;
		this.reason = reason;
		this.conditions = conditions;
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public Predicate<ModVersion> predicate() {
		return this.predicate;
	}

	@Override
	public String reason() {
		return this.reason;
	}

	@Override
	public Collection<Condition> conditions() {
		return this.conditions;
	}

	public static Incompatibility parse(String id, JsonObject json) throws JsonException {
		JsonString predicateJson = json.getOrThrow("predicate").asString();
		String reason = json.getOrThrow("reason").asString().value();
		Collection<Condition> conditions = ConditionRegistry.parse(json.get("conditions"));
		try {
			Predicate<ModVersion> parsed = ModVersion.parsePredicate(predicateJson.value());
			return new IncompatibilityImpl(id, parsed, reason, conditions);
		} catch (VersionPredicateSyntaxException e) {
			throw new JsonException(predicateJson, e.getMessage());
		}
	}
}
