package io.github.cichlidmc.cichlid.impl.loading.plugin;

import io.github.cichlidmc.cichlid.api.plugin.CichlidPlugin;
import io.github.cichlidmc.cichlid.impl.loaded.PluginImpl;
import io.github.cichlidmc.cichlid.impl.metadata.PluginMetadata;
import io.github.cichlidmc.cichlid.impl.report.ProblemReport;
import io.github.cichlidmc.cichlid.impl.report.ReportDetail;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Optional;
import java.util.jar.JarFile;

public abstract class LoadablePlugin {
	public final PluginMetadata metadata;
	public final String source;

	protected LoadablePlugin(PluginMetadata metadata, String source) {
		this.metadata = metadata;
		this.source = source;
	}

	public abstract Optional<LoadedPlugin> load(Instrumentation instrumentation, ProblemReport report);

	protected final Optional<LoadedPlugin> createFromClass(ProblemReport report) {
		try {
			Class<?> clazz = Class.forName(this.metadata.className);
			Constructor<?> constructor = clazz.getConstructor();
			CichlidPlugin instance = (CichlidPlugin) constructor.newInstance();
			LoadedPlugin loaded = new LoadedPlugin(instance, new PluginImpl(this.metadata), this.source);
			return Optional.of(loaded);
		} catch (ClassCastException e) {
			report.addSection("Plugin does not implement CichlidPlugin", this.classNameDetail());
		} catch (ClassNotFoundException e) {
			report.addSection("Plugin class does not exist", this.classNameDetail());
		} catch (NoSuchMethodException | IllegalAccessException e) {
			report.addSection("Plugin does not have a public no-arg constructor", this.classNameDetail());
		} catch (InstantiationException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return Optional.empty();
	}

	private ReportDetail classNameDetail() {
		return new ReportDetail("Class Name", this.metadata.className);
	}

	public static final class BuiltIn extends LoadablePlugin {
		public static final String BUILT_IN_SOURCE = "<built-in>";

		public final LoadedPlugin loaded;

		public BuiltIn(PluginMetadata metadata, CichlidPlugin plugin) {
			super(metadata, BUILT_IN_SOURCE);
			this.loaded = new LoadedPlugin(plugin, new PluginImpl(metadata), BUILT_IN_SOURCE);
		}

		@Override
		public Optional<LoadedPlugin> load(Instrumentation instrumentation, ProblemReport report) {
			return Optional.of(this.loaded);
		}
	}

	public static final class Jar extends LoadablePlugin {
		private final JarFile jar;

		public Jar(PluginMetadata metadata, JarFile jar) {
			super(metadata, jar.getName());
			this.jar = jar;
		}

		@Override
		public Optional<LoadedPlugin> load(Instrumentation instrumentation, ProblemReport report) {
			instrumentation.appendToSystemClassLoaderSearch(this.jar);
			return this.createFromClass(report);
		}
	}

	public static final class Classpath extends LoadablePlugin {
		public Classpath(PluginMetadata metadata, URI metadataLocation) {
			super(metadata, metadataLocation.toString());
		}

		@Override
		public Optional<LoadedPlugin> load(Instrumentation instrumentation, ProblemReport report) {
			return this.createFromClass(report);
		}
	}
}
