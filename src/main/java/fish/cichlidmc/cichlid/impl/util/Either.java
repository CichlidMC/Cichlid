package fish.cichlidmc.cichlid.impl.util;

import java.util.NoSuchElementException;

public interface Either<L, R> {
	static <L, R> Either<L, R> left(L value) {
		return new Left<>(value);
	}

	static <L, R> Either<L, R> right(R value) {
		return new Right<>(value);
	}

	default L left() {
		throw new NoSuchElementException();
	}

	default R right() {
		throw new NoSuchElementException();
	}

	default boolean isLeft() {
		return false;
	}

	default boolean isRight() {
		return false;
	}

	class Left<L, R> implements Either<L, R> {
		private final L value;

		public Left(L value) {
			this.value = value;
		}

		@Override
		public L left() {
			return this.value;
		}

		@Override
		public boolean isLeft() {
			return true;
		}
	}

	class Right<L, R> implements Either<L, R> {
		private final R value;

		public Right(R value) {
			this.value = value;
		}

		@Override
		public R right() {
			return this.value;
		}

		@Override
		public boolean isRight() {
			return true;
		}
	}
}
