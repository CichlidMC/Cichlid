package fish.cichlidmc.cichlid.api.mod.entrypoint;

import fish.cichlidmc.cichlid.api.loaded.Mod;

/**
 * An exception thrown during entrypoint invocation.
 */
public final class EntrypointException extends RuntimeException {
	public final String key;
	public final Mod mod;

	public EntrypointException(Mod mod, String key, Throwable cause) {
		super(makeMessage(mod, key), cause);
		this.key = key;
		this.mod = mod;
	}

	private static String makeMessage(Mod mod, String key) {
		return "Error invoking entrypoint '" + key + "' on mod " + mod.metadata().blame();
	}
}
