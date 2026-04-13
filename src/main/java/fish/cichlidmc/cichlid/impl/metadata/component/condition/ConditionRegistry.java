package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.Cichlid;
import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.fishflakes.api.value.Result;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class ConditionRegistry {
	public static final SimpleRegistry<MapCodec<? extends Condition>> INSTANCE = SimpleRegistry.create(Cichlid.ID);
	public static final Codec<Condition> CODEC = Codec.codecDispatch(INSTANCE.byIdCodec(), Condition::codec);
	public static final Codec<List<Condition>> LIST_CODEC = CODEC.listOf();

	public static Collection<Condition> parse(@Nullable JsonValue json) throws JsonException {
		if (json == null) {
			return List.of();
		}

		return switch (LIST_CODEC.decode(json)) {
			case Result.Success(List<Condition> value) -> value;
			case Result.Error(String message) -> throw new JsonException("Failed to decode conditions: " + message);
		};
	}

	public static void bootstrap() {
		INSTANCE.register(id("distribution"), DistCondition.CODEC);
		INSTANCE.register(id("mod_is_loaded"), ModIsLoadedCondition.CODEC);
		INSTANCE.register(id("plugin_is_loaded"), PluginIsLoadedCondition.CODEC);
		INSTANCE.register(id("not"), NotCondition.CODEC);
		INSTANCE.register(id("or"), OrCondition.CODEC);
	}

	private static Id id(String name) {
		return new Id(Cichlid.ID, name);
	}
}
