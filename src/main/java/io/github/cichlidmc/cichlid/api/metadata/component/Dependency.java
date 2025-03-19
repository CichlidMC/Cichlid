package io.github.cichlidmc.cichlid.api.metadata.component;

import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A Dependency represents a mod or plugin required by another mod or plugin.
 * Mods can depend on plugins and mods, but plugins can only depend on other plugins.
 */
@ApiStatus.NonExtendable
public interface Dependency {
	String id();

	/**
	 * User-friendly name of the required dependency.
	 */
	String name();

	/**
	 * Predicate for versions that match this dependency.
	 */
	VersionPredicate predicate();

	/**
	 * Optional string describing where to find this dependency.
	 * Should usually be a URL to a mod page, but could be something else, like GitHub Releases, or a Discord Invite.
	 * <strong>Never</strong> link directly to a file download.
	 * If this string is determined to be a direct download link, an error will be thrown.
	 */
	@Nullable
	String source();
}
