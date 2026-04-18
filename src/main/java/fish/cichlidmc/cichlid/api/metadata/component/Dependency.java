package fish.cichlidmc.cichlid.api.metadata.component;

import fish.cichlidmc.cichlid.api.version.ModVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/// A Dependency represents a mod or plugin required by another mod or plugin.
/// Mods can depend on plugins and mods, but plugins can only depend on other plugins.
@ApiStatus.NonExtendable
public interface Dependency {
	String id();

	/// User-friendly name of the required dependency.
	String name();

	/// Predicate for versions that match this dependency.
	Predicate<ModVersion> predicate();

	/// Optional string describing where to find this dependency.
	/// Should usually be a URL to a mod page, but could be something else, like GitHub Releases, or a Discord Invite.
	/// **Never** link directly to a file download.
	/// If this string is determined to be a direct download link, an error will be thrown during parsing.
	@Nullable
	String source();

	/// Conditions that must all match for this dependency to be considered.
	/// May be empty.
	Collection<Condition> conditions();
}
