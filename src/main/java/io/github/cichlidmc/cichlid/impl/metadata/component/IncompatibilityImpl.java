package io.github.cichlidmc.cichlid.impl.metadata.component;

import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import io.github.cichlidmc.cichlid.api.version.VersionPredicateSyntaxException;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;
import io.github.cichlidmc.tinyjson.value.primitive.JsonString;

public class IncompatibilityImpl implements Incompatibility {
	private final String id;
	private final VersionPredicate predicate;
	private final String reason;

	public IncompatibilityImpl(String id, VersionPredicate predicate, String reason) {
		this.id = id;
		this.predicate = predicate;
		this.reason = reason;
	}

	public static Incompatibility parse(JsonObject json) throws JsonException {
		String id = json.get("id").asString().value();
		JsonString predicateJson = json.get("predicate").asString();
		String reason = json.get("reason").asString().value();
		try {
			VersionPredicate parsed = VersionPredicate.parse(predicateJson.value());
			return new IncompatibilityImpl(id, parsed, reason);
		} catch (VersionPredicateSyntaxException e) {
			throw new JsonException(predicateJson, e.getMessage());
		}
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public VersionPredicate predicate() {
		return predicate;
	}

	@Override
	public String reason() {
		return reason;
	}
}
