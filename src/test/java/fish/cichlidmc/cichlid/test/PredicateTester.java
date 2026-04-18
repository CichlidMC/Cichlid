package fish.cichlidmc.cichlid.test;

import org.junit.jupiter.api.Assertions;

import java.util.function.Function;
import java.util.function.Predicate;

public record PredicateTester<T>(Predicate<T> predicate) {
	public <R> PredicateTester<R> map(Function<R, T> function) {
		return new PredicateTester<>(r -> this.predicate.test(function.apply(r)));
	}

	public void assertMatches(T... values) {
		for (T value : values) {
			Assertions.assertTrue(this.predicate.test(value), value::toString);
		}
	}

	public void assertNotMatching(T... values) {
		for (T value : values) {
			Assertions.assertFalse(this.predicate.test(value), value::toString);
		}
	}
}
