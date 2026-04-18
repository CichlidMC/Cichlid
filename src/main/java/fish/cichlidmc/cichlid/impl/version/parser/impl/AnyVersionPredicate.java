package fish.cichlidmc.cichlid.impl.version.parser.impl;

import java.util.function.Predicate;

public enum AnyVersionPredicate implements Predicate<Object> {
	INSTANCE;

	@Override
	public boolean test(Object o) {
		return true;
	}

	@Override
	public String toString() {
		return "any";
	}

	@SuppressWarnings("unchecked")
	public <T> Predicate<T> cast() {
		return (Predicate<T>) this;
	}
}
