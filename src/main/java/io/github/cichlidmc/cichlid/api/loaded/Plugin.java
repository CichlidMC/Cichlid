package io.github.cichlidmc.cichlid.api.loaded;

import io.github.cichlidmc.cichlid.api.metadata.Metadata;
import org.jetbrains.annotations.ApiStatus;

/**
 * A plugin that has been loaded by Cichlid.
 */
@ApiStatus.NonExtendable
public interface Plugin {
	Metadata metadata();
}
