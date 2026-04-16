package fish.cichlidmc.cichlid.impl.logging.backend;

import fish.cichlidmc.cichlid.api.CichlidPaths;
import fish.cichlidmc.cichlid.impl.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class FallbackLoggerBackend implements LoggerBackend {
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

	public FallbackLoggerBackend(String name) {
		this.name = name;
	}

	@Override
	public void write(String message, Level level) {
		String time = timeFormat.format(new Date());
		String thread = Thread.currentThread().getName();
		String formatted = String.format(format, time, thread, this.name, level, message);
		this.writeRaw(formatted);
	}

	@Override
	public void write(Throwable throwable) {
		String string = Utils.getStackTrace(throwable);
		for (String line : string.split(System.lineSeparator())) {
			this.write(line, Level.ERROR);
		}
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
