package io.github.cichlidmc.cichlid.impl.metadata.component.condition;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.tinycodecs.map.MapCodec;

public final class PluginIsLoadedCondition implements Condition {
	public static final MapCodec<PluginIsLoadedCondition> CODEC = Metadata.ID_CODEC.xmap(
			PluginIsLoadedCondition::new, condition -> condition.id
	).fieldOf("id");

	private final String id;

	public PluginIsLoadedCondition(String id) {
		this.id = id;
	}

	@Override
	public boolean matches(Context context) {
		return context.isModPresent(this.id);
	}

	@Override
	public MapCodec<? extends Condition> codec() {
		return CODEC;
	}
}
