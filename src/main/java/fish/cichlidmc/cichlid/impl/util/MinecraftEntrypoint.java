package fish.cichlidmc.cichlid.impl.util;

import fish.cichlidmc.cichlid.api.dist.Distribution;

public enum MinecraftEntrypoint {
	CLIENT_MAIN(Distribution.CLIENT, "net.minecraft.client.main.Main"),
	SERVER_MAIN(Distribution.DEDICATED_SERVER, "net.minecraft.server.Main"),
	SERVER_BUNDLER(Distribution.DEDICATED_SERVER, "net.minecraft.bundler.Main");

	public final Distribution distribution;
	public final String className;

	MinecraftEntrypoint(Distribution distribution, String className) {
		this.distribution = distribution;
		this.className = className;
	}

	public boolean detect(ClassLoader loader) {
		String resourcePath = this.className.replace('.', '/') + ".class";
		return loader.getResource(resourcePath) != null;
	}
}
