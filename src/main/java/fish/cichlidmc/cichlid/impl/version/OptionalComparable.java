package fish.cichlidmc.cichlid.impl.version;

import java.util.OptionalInt;

/// A variant of a [Comparable] that may not be comparable to some values.
@FunctionalInterface
public interface OptionalComparable<T> {
	OptionalInt compareTo(T that);

	static <T> OptionalComparable<T> of(Comparable<T> comparable) {
		return that -> OptionalInt.of(comparable.compareTo(that));
	}
}
