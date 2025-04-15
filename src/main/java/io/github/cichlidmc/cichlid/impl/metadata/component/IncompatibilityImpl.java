package io.github.cichlidmc.cichlid.impl.metadata.component;

import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.cichlid.impl.metadata.component.condition.ConditionRegistry;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;

import java.util.Collection;

public class IncompatibilityImpl implements Incompatibility {
	private final String id;
	private final VersionPredicate predicate;
	private final String reason;
	private final Collection<Condition> conditions;

	public IncompatibilityImpl(String id, VersionPredicate predicate, String reason, Collection<Condition> conditions) {
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
	public VersionPredicate predicate() {
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
		JsonString predicateJson = json.get("predicate").asString();
		String reason = json.get("reason").asString().value();
		Collection<Condition> conditions = ConditionRegistry.parse(json.getNullable("conditions"));
		try {
			VersionPredicate parsed = VersionPredicate.parse(predicateJson.value());
			return new IncompatibilityImpl(id, parsed, reason, conditions);
		} catch (VersionPredicateSyntaxException e) {
			throw new JsonException(predicateJson, e.getMessage());
		}
	}
}
