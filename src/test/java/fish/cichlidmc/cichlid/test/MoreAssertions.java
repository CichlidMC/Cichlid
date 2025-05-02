package fish.cichlidmc.cichlid.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

public class MoreAssertions {
	public static <T extends Throwable> void assertThrowsWithMessage(Class<T> exceptionClass, String message, Executable function) {
		T throwable = Assertions.assertThrows(exceptionClass, function);
		Assertions.assertEquals(message, throwable.getMessage());
	}
}
