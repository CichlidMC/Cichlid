package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.metadata.Metadata;
import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.tinycodecs.map.MapCodec;

public final class ModIsLoadedCondition implements Condition {
	public static final MapCodec<ModIsLoadedCondition> CODEC = Metadata.ID_CODEC.xmap(
			ModIsLoadedCondition::new, condition -> condition.id
	).fieldOf("id");

	private final String id;

	public ModIsLoadedCondition(String id) {
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
