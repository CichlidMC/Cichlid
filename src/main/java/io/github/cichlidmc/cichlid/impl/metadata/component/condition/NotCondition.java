package io.github.cichlidmc.cichlid.impl.metadata.component.condition;

import io.github.cichlidmc.cichlid.api.metadata.component.Condition;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;

public final class NotCondition implements Condition {
	public static final MapCodec<NotCondition> CODEC = Codec.lazy(() -> ConditionRegistry.CODEC.xmap(
			NotCondition::new, condition -> condition.wrapped
	)).fieldOf("condition");

	private final Condition wrapped;

	public NotCondition(Condition wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean matches(Context context) {
		return !this.wrapped.matches(context);
	}

	@Override
	public MapCodec<? extends Condition> codec() {
		return CODEC;
	}
}
