package io.github.cichlidmc.cichlid.impl;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ProblemReportTextRenderer;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;
import io.github.cichlidmc.cichlid.impl.report.ReportSection;
import io.github.cichlidmc.cichlid.impl.report.ReportedException;
import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import io.github.cichlidmc.cichlid.impl.transformer.ClassPoisoner;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Consumer;

public class CichlidAgent {
	private static final Path catastropheLog = CichlidPaths.CICHLID_ROOT.resolve("catastrophe.txt");

	private static final List<Path> importantDirectories = Utils.listOf(
			CichlidPaths.MODS, CichlidPaths.CONFIGS, CichlidPaths.PLUGINS
	);

	private static final List<Class<?>> criticalClasses = Utils.listOf(
			CichlidLogger.class, CichlidTransformer.class, CatastropheLogger.class, ClassPoisoner.class,
			ProblemReport.class, ReportSection.class, ReportDetail.class, ReportedException.class
	);

	public static void premain(@Nullable String args, Instrumentation instrumentation) {
		try {
			setupFiles();
			preloadCriticalClasses();
			CichlidImpl.load(args, instrumentation);
		} catch (Throwable t) {
			// in case of a rogue transformer breaking everything, stop transforming classes
			CichlidTransformer.emergencyStop();

			try {
				handleError(t);
			} catch (Throwable t2) {
				// uh oh. An agent throwing an error is pretty messy, do our best to avoid it
				t.addSuppressed(t2);
				handleCatastrophe(t);
			}

			// cancel the launch
			System.exit(1);
		}
	}

	private static void handleError(Throwable t) {
		CichlidLogger logger = CichlidLogger.get("Cichlid");
		ProblemReport report = ProblemReport.of(t);
		logger.error("One or more errors occurred while loading Cichlid.");
		logger.info(ProblemReportTextRenderer.render(report));
		// TODO: GUI
	}

	private static void handleCatastrophe(Throwable t) {
		CatastropheLogger logger = new CatastropheLogger(t::addSuppressed);

		logger.println("Something went *very* wrong while loading Cichlid!");
		logger.println("Please report this at https://github.com/CichlidMC/Cichlid/issues");
		logger.printThrowable(t);
	}

	private static void setupFiles() throws IOException {
		Files.deleteIfExists(catastropheLog);
		for (Path directory : importantDirectories) {
			Files.createDirectories(directory);
		}
	}

	private static void preloadCriticalClasses() {
		criticalClasses.forEach(CichlidAgent::preload);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void preload(Class<?> clazz) {
		clazz.getDeclaredMethods();
	}

	private static final class CatastropheLogger {
		private final PrintStream console;
		private final PrintStream log;

		private CatastropheLogger(Consumer<Throwable> suppressedErrors) {
			this.console = System.err;
			this.log = new PrintStream(getLogOutputStream(suppressedErrors));
		}

		private void println(String s) {
			this.console.println(s);
			this.log.println(s);
		}

		private void printThrowable(Throwable t) {
			t.printStackTrace(this.console);
			t.printStackTrace(this.log);
		}

		private static OutputStream getLogOutputStream(Consumer<Throwable> suppressedErrors) {
			try {
				return Files.newOutputStream(catastropheLog, StandardOpenOption.CREATE);
			} catch (Throwable t) {
				suppressedErrors.accept(t);
				// welp. to the shredder
				return new ByteArrayOutputStream();
			}
		}
	}
}
