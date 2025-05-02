package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.CodecResult;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import fish.cichlidmc.tinyjson.JsonException;
import fish.cichlidmc.tinyjson.value.JsonValue;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ConditionRegistry {
	public static final SimpleRegistry<MapCodec<? extends Condition>> INSTANCE = SimpleRegistry.create(ConditionRegistry::bootstrap);
	public static final Codec<Condition> CODEC = Codec.codecDispatch(INSTANCE.byIdCodec(), Condition::codec);
	public static final Codec<List<Condition>> LIST_CODEC = CODEC.listOf();

	public static Collection<Condition> parse(@Nullable JsonValue json) throws JsonException {
		if (json == null) {
			return Collections.emptyList();
		}

		CodecResult<List<Condition>> result = LIST_CODEC.decode(json);
		if (result.isError()) {
			throw new JsonException("Failed to decode conditions: " + result.asError().message);
		}

		return result.asSuccess().getOrThrow();
	}

	private static void bootstrap(SimpleRegistry.Builder<MapCodec<? extends Condition>> builder) {
		builder.register(id("distribution"), DistCondition.CODEC);
		builder.register(id("mod_is_loaded"), ModIsLoadedCondition.CODEC);
		builder.register(id("plugin_is_loaded"), PluginIsLoadedCondition.CODEC);
		builder.register(id("not"), NotCondition.CODEC);
		builder.register(id("or"), OrCondition.CODEC);
	}

	private static Id id(String name) {
		return new Id(CichlidImpl.ID, name);
	}
}
