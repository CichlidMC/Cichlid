package io.github.cichlidmc.cichlid.impl.util;

import io.github.cichlidmc.cichlid.api.loaded.LoadedSet;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class LoadedSetImpl<T> extends AbstractCollection<T> implements LoadedSet<T> {
	private final Map<String, T> byId;

	public LoadedSetImpl(Map<String, T> byId) {
		this.byId = Collections.unmodifiableMap(byId);
	}

	@Override
	public boolean isLoaded(String id) {
		return this.byId.containsKey(id);
	}

	@Override
	public Optional<T> get(String id) {
		return Optional.ofNullable(this.byId.get(id));
	}

	@Override
	public T getOrThrow(String id) {
		T value = this.byId.get(id);
		if (value == null) {
			throw new NoSuchElementException(id + " is not loaded!");
		}
		return value;
	}

	@Override
	public Iterator<T> iterator() {
		return this.byId.values().iterator();
	}

	@Override
	public int size() {
		return this.byId.size();
	}
}
