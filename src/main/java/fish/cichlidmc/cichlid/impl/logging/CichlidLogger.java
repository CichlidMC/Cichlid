package fish.cichlidmc.cichlid.impl.logging;

public sealed interface CichlidLogger permits CichlidLoggerImpl {
	void space();
	void info(String message);
	void warn(String message);
	void error(String message);
	void throwable(Throwable t);

	static CichlidLogger get(Class<?> clazz) {
		return get(clazz.getSimpleName());
	}

	static CichlidLogger get(String name) {
		return new CichlidLoggerImpl(name);
	}
}
