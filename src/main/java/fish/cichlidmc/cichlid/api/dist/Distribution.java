package fish.cichlidmc.cichlid.api.dist;

import fish.cichlidmc.cichlid.impl.CichlidImpl;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/// The two distributions of Minecraft.
public enum Distribution {
	CLIENT("Client"),
	DEDICATED_SERVER("Dedicated Server");

	public static final Codec<Distribution> CODEC = Codec.byName(Distribution.class, dist -> dist.serializedName);

	public final String prettyName;
	/// The name of this distribution in `snake_case`, for use in serialization.
	public final String serializedName;

	Distribution(String prettyName) {
		this.prettyName = prettyName;
		this.serializedName = this.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return this.prettyName;
	}

	/// Try to parse a Distribution from the given `snake_case` name, returning null if invalid.
	@Nullable
	public static Distribution of(String name) {
		return switch(name) {
			case "client" -> CLIENT;
			case "dedicated_server" -> DEDICATED_SERVER;
			default -> null;
		};
	}

	/// @return the currently loaded Distribution of Minecraft
	public static Distribution current() {
		return CichlidImpl.DISTRIBUTION.get();
	}
}
