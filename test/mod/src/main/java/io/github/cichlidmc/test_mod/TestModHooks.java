package io.github.cichlidmc.test_mod;

public final class TestModHooks {
	public static void onMain() {
		System.out.println("This method call was injected by Sushi!");
	}

	public void thing(String s) {
		if (!"test".equals(s)) {
			Object object = h(String.class);
			if (object != null) {
				s = object.toString();
			}
		}


		System.out.println(s);
	}

	private static Object h(Object... os) {
		return null;
	}
}
