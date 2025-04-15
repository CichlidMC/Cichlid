package io.github.cichlidmc.cichlid.impl.metadata.component.condition;

import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.cichlid.impl.CichlidImpl;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.CodecResult;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import io.github.cichlidmc.tinyjson.JsonException;
import io.github.cichlidmc.tinyjson.value.JsonValue;
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

	private static void bootstrap(SimpleRegistry<MapCodec<? extends Condition>> registry) {
		registry.register(id("distribution"), DistCondition.CODEC);
		registry.register(id("mod_is_loaded"), ModIsLoadedCondition.CODEC);
		registry.register(id("plugin_is_loaded"), PluginIsLoadedCondition.CODEC);
		registry.register(id("not"), NotCondition.CODEC);
		registry.register(id("or"), OrCondition.CODEC);
	}

	private static Id id(String name) {
		return new Id(CichlidImpl.ID, name);
	}
}
