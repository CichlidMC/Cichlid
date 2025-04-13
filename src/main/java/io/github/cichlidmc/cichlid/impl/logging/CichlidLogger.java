package io.github.cichlidmc.cichlid.impl.logging;

import io.github.cichlidmc.cichlid.impl.logging.impl.FallbackLoggerImpl;
import io.github.cichlidmc.cichlid.impl.logging.impl.Log4jLoggerImpl;
import io.github.cichlidmc.cichlid.impl.util.Utils;

import java.util.function.Function;

public interface CichlidLogger {
	Function<String, CichlidLogger> FACTORY = Utils.make(() -> {
		try {
			Class.forName("org.apache.logging.log4j.Logger");
			return Log4jLoggerImpl::new;
		} catch (Throwable t) {
			FallbackLoggerImpl logger = new FallbackLoggerImpl(CichlidLogger.class.getSimpleName());
			logger.info("Using fallback logger");
			return FallbackLoggerImpl::new;
		}
	});

	void space();
	void info(String message);
	void warn(String message);
	void error(String message);
	void throwable(Throwable t);

	static CichlidLogger get(Class<?> clazz) {
		return get(clazz.getSimpleName());
	}

	static CichlidLogger get(String name) {
		return FACTORY.apply(name);
	}
}
