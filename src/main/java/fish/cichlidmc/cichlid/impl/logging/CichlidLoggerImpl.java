package fish.cichlidmc.cichlid.impl.logging;

import fish.cichlidmc.cichlid.impl.logging.backend.FallbackLoggerBackend;
import fish.cichlidmc.cichlid.impl.logging.backend.Log4jLoggerBackend;
import fish.cichlidmc.cichlid.impl.logging.backend.LoggerBackend;

public final class CichlidLoggerImpl implements CichlidLogger {
	public static final String LOGGER_CLASS_FILE_NAME = "org/apache/logging/log4j/Logger.class";
	private static boolean log4jReady = isLog4jReady();

	private final String name;
	private LoggerBackend backend;

	public CichlidLoggerImpl(String name) {
		this.name = name;
		this.backend = log4jReady ? new Log4jLoggerBackend(name) : new FallbackLoggerBackend(name);
	}

	@Override
	public void space() {
		this.info("");
	}

	@Override
	public void info(String message) {
		this.checkBackend();
		this.backend.write(message, LoggerBackend.Level.INFO);
	}

	@Override
	public void warn(String message) {
		this.checkBackend();
		this.backend.write(message, LoggerBackend.Level.WARN);
	}

	@Override
	public void error(String message) {
		this.checkBackend();
		this.backend.write(message, LoggerBackend.Level.ERROR);
	}

	@Override
	public void throwable(Throwable t) {
		this.checkBackend();
		this.backend.write(t);
	}

	private void checkBackend() {
		boolean wasLog4jReady = log4jReady;
		checkLog4jReady();

		if (log4jReady) {
			if (this.backend instanceof FallbackLoggerBackend) {
				this.backend = new Log4jLoggerBackend(this.name);
			}

			if (!wasLog4jReady) {
				this.info("Log4j is now ready. Switched to Log4jLoggerBackend.");
			}
		}
	}

	private static void checkLog4jReady() {
		if (log4jReady)
			return;

		if (isLog4jReady()) {
			log4jReady = true;
		}
	}

	private static boolean isLog4jReady() {
		// we need to look up the class file instead of trying to load the class,
		// since if a class fails to load once the JVM is supposed to cache that.
		ClassLoader loader = CichlidLoggerImpl.class.getClassLoader();
		return loader.getResource(LOGGER_CLASS_FILE_NAME) != null;
	}
}
