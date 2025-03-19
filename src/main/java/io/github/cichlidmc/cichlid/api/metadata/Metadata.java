package io.github.cichlidmc.cichlid.api.metadata;

import io.github.cichlidmc.cichlid.api.metadata.component.Dependency;
import io.github.cichlidmc.cichlid.api.metadata.component.Incompatibility;
import io.github.cichlidmc.cichlid.api.version.Version;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Metadata shared by mods and plugins.
 */
@ApiStatus.NonExtendable
public interface Metadata {
	/**
	 * Regex for valid IDs. No length limit, cannot be empty. Valid characters: a-z, 0-9, and _.
	 */
	Pattern ID_REGEX = Pattern.compile("[a-z0-9_]+");
	
	/**
	 * The unique ID of this mod or plugin. Only one mod or plugin with a given ID can be loaded.
	 *
	 * @see #ID_REGEX
	 */
	String id();

	/**
	 * The fully formatted name. Only disallowed characters are line breaks.
	 */
	String name();

	/**
	 * The currently loaded version.
	 */
	Version version();

	/**
	 * Description. No restrictions, may be multi-line. Optional, may be empty.
	 */
	String description();

	/**
	 * Ordered map of names to arbitrary roles. Common credits: Author, Contributor, Artist, Inspiration.
	 */
	Map<String, String> credits();

	/**
	 * Map of IDs to provided versions. Mods can only provide other mods, likewise for plugins.
	 */
	Map<String, Version> provides();

	/**
	 * Map of IDs to dependencies.
	 * Both mods and plugins can depend on other plugins, but only mods can depend on mods.
	 */
	Map<String, Dependency> dependencies();

	/**
	 * Map of IDs to incompatibilities.
	 * Mods and plugins can be incompatible with each other, unlike dependencies.
	 */
	Map<String, Incompatibility> incompatibilities();

	/**
	 * A formatted string that contains both the name and ID of this mod or plugin.
	 * This is useful for clearly showing a mod to users, ex. in the case of an error.
	 * <br>
	 * Example: {@code My Mod (ID: mymod)}
	 */
	default String blame() {
		return this.name() + " (ID: " + this.id() + ")";
	}

	/**
	 * Check if the given ID is valid.
	 */
	static boolean isValidId(String id) {
		return ID_REGEX.matcher(id).matches();
	}
}
