package io.github.cichlidmc.cichlid.impl.loading;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.impl.util.Either;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.TinyJson;
import io.github.cichlidmc.tinyjson.value.JsonValue;
import io.github.cichlidmc.tinyjson.value.composite.JsonObject;

import java.io.IOException;
import java.net.URI;

/**
 * Common loading code shared between plugins and mods.
 */
public final class SharedLoading {
	public static <M extends Metadata> Either<M, JsonException> parseMetadata(URI uri, MetadataParser<M> parser) throws IOException {
		try {
			JsonValue json = TinyJson.fetch(uri);
			M metadata = parser.parse(json.asObject());
			return Either.left(metadata);
		} catch (JsonException e) {
			return Either.right(e);
		}
	}

	@FunctionalInterface
	public interface MetadataParser<M extends Metadata> {
		M parse(JsonObject json) throws JsonException;
	}
}
