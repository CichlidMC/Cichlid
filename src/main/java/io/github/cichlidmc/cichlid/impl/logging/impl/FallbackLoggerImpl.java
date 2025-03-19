package io.github.cichlidmc.cichlid.impl.logging.impl;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FallbackLoggerImpl implements CichlidLogger {
	private static final Path file = CichlidPaths.CICHLID_ROOT.resolve("log.txt");
	private static final String format = "[%s] [%s] [%s] [%s]: %s";
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	static {
		try {
			// reset log on init
			Files.deleteIfExists(file);
			Files.createDirectories(file.getParent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final String name;

	public FallbackLoggerImpl(String name) {
		this.name = name;
	}

	@Override
	public void space() {
		this.writeRaw("");
	}

	@Override
	public void info(String message) {
		this.write("INFO", message);
	}

	@Override
	public void warn(String message) {
		this.write("WARN", message);
	}

	@Override
	public void error(String message) {
		this.write("ERROR", message);
	}

	@Override
	public void throwable(Throwable t) {
		String string = Utils.getStackTrace(t);
		for (String line : string.split(System.lineSeparator())) {
			this.error(line);
		}
	}

	private void write(String level, String message) {
		String time = timeFormat.format(new Date());
		String thread = Thread.currentThread().getName();
		String formatted = String.format(format, time, thread, this.name, level, message);
		this.writeRaw(formatted);
	}

	private void writeRaw(String string) {
		System.out.println(string);
		try {
			byte[] bytes = (string + '\n').getBytes();
			Files.write(file, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
