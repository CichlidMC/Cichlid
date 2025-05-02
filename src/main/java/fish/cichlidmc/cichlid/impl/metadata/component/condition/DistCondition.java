package fish.cichlidmc.cichlid.impl.metadata.component.condition;

import fish.cichlidmc.cichlid.api.Cichlid;
import fish.cichlidmc.cichlid.api.dist.Distribution;
import fish.cichlidmc.cichlid.api.metadata.component.Condition;
import fish.cichlidmc.tinycodecs.map.MapCodec;

public final class DistCondition implements Condition {
	public static final MapCodec<DistCondition> CODEC = Distribution.CODEC.xmap(
			DistCondition::new, condition -> condition.distribution
	).fieldOf("distribution");

	private final Distribution distribution;

	public DistCondition(Distribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public boolean matches(Context context) {
		return this.distribution == Cichlid.distribution();
	}

	@Override
	public MapCodec<? extends Condition> codec() {
		return CODEC;
	}
}
