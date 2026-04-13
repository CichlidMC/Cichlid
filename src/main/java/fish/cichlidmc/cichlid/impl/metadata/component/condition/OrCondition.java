package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.List;

public final class OrCondition implements Condition {
	public static final MapCodec<OrCondition> CODEC = Codec.lazy(() -> ConditionRegistry.CODEC.listOf().xmap(
			OrCondition::new, condition -> condition.conditions
	)).fieldOf("conditions");

	private final List<Condition> conditions;

	public OrCondition(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean matches(Context context) {
		for (Condition condition : this.conditions) {
			if (condition.matches(context)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public MapCodec<? extends Condition> codec() {
		return CODEC;
	}
}
