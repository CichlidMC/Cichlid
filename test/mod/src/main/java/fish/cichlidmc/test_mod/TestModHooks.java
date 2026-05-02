package fish.cichlidmc.test_mod;

import java.lang.reflect.Method;
import java.util.List;

public final class TestModHooks {
	public static List<Object> overrideSplashes(List<Object> original) {
		try {
			Class<?> splashManager = Class.forName("net.minecraft.client.resources.SplashManager");
			Class<?> component = Class.forName("net.minecraft.network.chat.Component");
			Method literalSplash = splashManager.getDeclaredMethod("literalSplash", String.class);
			literalSplash.setAccessible(true);
			return List.of(literalSplash.invoke(null, "Fish!"));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
