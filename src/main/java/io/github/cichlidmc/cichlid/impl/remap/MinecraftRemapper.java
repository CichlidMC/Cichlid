package io.github.cichlidmc.cichlid.impl.remap;

import io.github.cichlidmc.cichlid.api.CichlidPaths;
import io.github.cichlidmc.cichlid.impl.logging.CichlidLogger;
import io.github.cichlidmc.cichlid.impl.util.Utils;
import net.neoforged.art.api.ClassProvider;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.internal.RenamingTransformer;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class MinecraftRemapper {
	public static final Path DIR = CichlidPaths.CACHE.resolve("remap");
	public static final Path REMAPPED_JAR = DIR.resolve("minecraft.jar");
	public static final Path LOCK = DIR.resolve(".lock");
	public static final Consumer<String> NOOP_LOGGER = message -> {};

	private static final CichlidLogger logger = CichlidLogger.get(MinecraftRemapper.class);

	public static void handle(IMappingFile mappings, Instrumentation instrumentation) {
		try {
			if (!Files.exists(REMAPPED_JAR))
				remap(mappings);

			JarFile file = new JarFile(REMAPPED_JAR.toFile());
			instrumentation.appendToSystemClassLoaderSearch(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to handle remapping", e);
		}
	}

	private static void remap(IMappingFile mappings) throws IOException {
		Files.createDirectories(DIR);
		if (!Files.exists(LOCK)) {
			// open option doesn't work for some reason
			Files.createFile(LOCK);
		}

		try (FileChannel channel = FileChannel.open(LOCK, StandardOpenOption.WRITE); FileLock ignored = channel.lock()) {
			if (Files.exists(REMAPPED_JAR))
				return;

			doRemap(mappings);
		}

		logger.info("Minecraft was successfully remapped.");
	}

	private static void doRemap(IMappingFile mappings) throws IOException {
		logger.info("Remapped Minecraft isn't cached. This could take a second...");

		ClassLoader loader = MinecraftRemapper.class.getClassLoader();
		ClassProvider provider = new ReadingClassProvider(loader);
		RenamingTransformer transformer = new RenamingTransformer(provider, mappings, NOOP_LOGGER, false);

		Files.createFile(REMAPPED_JAR);
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(REMAPPED_JAR))) {
			for (IMappingFile.IClass clazz : mappings.getClasses()) {
				Transformer.ClassEntry entry = readClassEntry(clazz.getOriginal());
				if (entry == null) {
					String string = clazz.getOriginal() + " -> " + clazz.getMapped();
					logger.warn("Class is specified by mappings, but wasn't found: " + string);
					continue;
				}

				Transformer.ClassEntry processed = transformer.process(entry);

				out.putNextEntry(new ZipEntry(processed.getName()));
				out.write(processed.getData());
			}

			// byte[] manifestBytes = findManifest();
			// if (manifestBytes != null) {
			// 	try (BufferedInputStream in = new BufferedInputStream(ByteArrayInputStreamm))
			// }

			out.finish();
		}
	}

	@Nullable
	private static Transformer.ClassEntry readClassEntry(String name) {
		byte[] bytes = Utils.readClassLoaderResource(null, name + ".class");
		if (bytes == null)
			return null;

		return Transformer.ClassEntry.create(name + ".class", Transformer.Entry.STABLE_TIMESTAMP, bytes);
	}

	// check all manifests, and look for one with an entry matching an unmapped class
	// private static List<String> findManifest(IMappingFile mappings) throws IOException {
	// 	Set<String> classes = mappings.getClasses().stream().map(IMappingFile.INode::getOriginal).collect(Collectors.toSet());
	//
	// 	Enumeration<URL> enumeration = MinecraftRemapper.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
	// 	while (enumeration.hasMoreElements()) {
	// 		URL url = enumeration.nextElement();
	// 		try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
	// 			for (String line = in.readLine(); line != null; line = in.readLine()) {
	// 				if (line.startsWith("Name: ")) {
	// 					for (String className : classes) {
	//
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// }
}
