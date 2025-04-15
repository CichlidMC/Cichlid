package io.github.cichlidmc.cichlid.api.metadata.component;

import io.github.cichlidmc.cichlid.api.version.VersionPredicate;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * An incompatibility represents a mod or plugin that is incompatible with another mod or plugin.
 */
@ApiStatus.NonExtendable
public interface Incompatibility {
	String id();

	/**
	 * Predicate for versions that match this incompatibility.
	 */
	VersionPredicate predicate();

	/**
	 * Reason why this incompatibility exists.
	 */
	String reason();

	/**
	 * Conditions that must all match for this incompatibility to be considered.
	 * May be empty.
	 */
	Collection<Condition> conditions();
}
