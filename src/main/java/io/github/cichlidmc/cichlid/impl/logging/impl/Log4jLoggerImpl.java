package io.github.cichlidmc.cichlid.impl.logging.impl;

import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jLoggerImpl implements CichlidLogger {
	private final Logger logger;

	public Log4jLoggerImpl(String name) {
		this.logger = LogManager.getLogger(name);
	}

	@Override
	public void info(String message) {
		this.logger.info(message);
	}

	@Override
	public void warn(String message) {
		this.logger.warn(message);
	}

	@Override
	public void error(String message) {
		this.logger.error(message);
	}

	@Override
	public void throwable(Throwable t) {
		this.logger.throwing(t);
	}
}
