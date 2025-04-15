package io.github.cichlidmc.cichlid.api.metadata.component;

import io.github.cichlidmc.cichlid.api.Cichlid;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.ApiStatus;

/**
 * Conditions for dependencies and incompatibilities.
 */
@ApiStatus.NonExtendable
public interface Condition {
	boolean matches(Context context);

	MapCodec<? extends Condition> codec();

	/**
	 * Context for Conditions, since they are queried while Cichlid is mid-initialization.
	 */
	@ApiStatus.NonExtendable
	interface Context {
		/**
		 * Context object that may be used after Cichlid is fully initialized.
		 */
		Context INITIALIZED = new Context() {
			@Override
			public boolean isPluginPresent(String id) {
				return Cichlid.plugins().isLoaded(id);
			}

			@Override
			public boolean isModPresent(String id) {
				return Cichlid.mods().isLoaded(id);
			}
		};

		boolean isPluginPresent(String id);

		boolean isModPresent(String id);
	}
}
