package fish.cichlidmc.cichlid.impl.loading;

import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.impl.util.Either;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;
import fish.cichlidmc.tinyjson.value.composite.JsonObject;

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
