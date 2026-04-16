package fish.cichlidmc.cichlid.impl.logging.backend;

public sealed interface LoggerBackend permits Log4jLoggerBackend, FallbackLoggerBackend {
	void write(String message, Level level);

	void write(Throwable throwable);

	enum Level {
		INFO, WARN, ERROR
	}
}
