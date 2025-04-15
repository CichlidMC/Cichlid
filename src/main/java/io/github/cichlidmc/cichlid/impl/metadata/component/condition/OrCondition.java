package io.github.cichlidmc.cichlid.impl.metadata.component.condition;

import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;

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
