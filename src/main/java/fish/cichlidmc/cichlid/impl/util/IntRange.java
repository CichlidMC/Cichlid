package fish.cichlidmc.cichlid.impl.util;

/// Both min and max are inclusive
public class IntRange {
	public final int min;
	public final int max;

	public IntRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public boolean contains(int i) {
		return i >= this.min && i <= this.max;
	}
}
