package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

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
