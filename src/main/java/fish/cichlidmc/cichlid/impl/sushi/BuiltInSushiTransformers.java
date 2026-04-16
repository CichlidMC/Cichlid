package fish.cichlidmc.cichlid.impl.sushi;

import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.fishflakes.api.value.Result;
import fish.cichlidmc.sushi.api.TransformerManager;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.phase.Phase;
import fish.cichlidmc.tinyjson.TinyJson;
import fish.cichlidmc.tinyjson.value.JsonValue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class BuiltInSushiTransformers {
	public static final Id PHASE = CichlidImpl.id("built_in");

	public static void register(TransformerManager.Builder builder, Path cichlidResources) throws IOException {
		Phase.Builder phase = builder.definePhaseOrThrow(PHASE);
		phase.runBefore(Phase.DEFAULT);
		phase.withBarriers(Phase.Barriers.AFTER_ONLY);

		Path transformers = cichlidResources.resolve("transformers");
		if (!Files.exists(transformers)) {
			throw new IllegalStateException("Built-in Sushi transformers are missing");
		}

		Files.walkFileTree(transformers, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String relative = transformers.relativize(file).toString();
				int dot = relative.lastIndexOf('.');
				String name = dot < 0 ? "" : relative.substring(0, dot);
				if (name.isBlank() || !Id.isValidPath(name)) {
					throw new IllegalArgumentException("Invalid name for transformer " + file);
				}

				JsonValue json = TinyJson.parse(file);
				Result<Transformer> result = Transformer.CODEC.decode(json);
				if (result instanceof Result.Error(String message)) {
					throw new IllegalStateException("Failed to parse built-in transformer " + name + ": " + message);
				}

				Id id = CichlidImpl.id(name);
				phase.registerOrThrow(new ConfiguredTransformer(id, result.valueOrThrow()));

				return FileVisitResult.CONTINUE;
			}
		});
	}
}
