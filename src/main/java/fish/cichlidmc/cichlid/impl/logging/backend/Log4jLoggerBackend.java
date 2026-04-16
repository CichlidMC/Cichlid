package fish.cichlidmc.cichlid.impl.logging.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log4jLoggerBackend implements LoggerBackend {
	private final Logger logger;

	public Log4jLoggerBackend(String name) {
		this.logger = LogManager.getLogger(name);
	}

	@Override
	public void write(String message, Level level) {
		switch (level) {
			case INFO -> this.logger.info(message);
			case WARN -> this.logger.warn(message);
			case ERROR -> this.logger.error(message);
		}
	}

	@Override
	public void write(Throwable throwable) {
		this.logger.throwing(throwable);
	}
}
