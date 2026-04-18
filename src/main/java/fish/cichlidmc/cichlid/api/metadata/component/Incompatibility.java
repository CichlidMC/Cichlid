package fish.cichlidmc.cichlid.api.metadata.component;

import fish.cichlidmc.cichlid.api.version.ModVersion;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Predicate;

/// An incompatibility represents a mod or plugin that is incompatible with another mod or plugin.
@ApiStatus.NonExtendable
public interface Incompatibility {
	String id();

	/// Predicate for versions that match this incompatibility.
	Predicate<ModVersion> predicate();

	/// Reason why this incompatibility exists.
	String reason();

	/// Conditions that must all match for this incompatibility to be considered.
	/// May be empty.
	Collection<Condition> conditions();
}
