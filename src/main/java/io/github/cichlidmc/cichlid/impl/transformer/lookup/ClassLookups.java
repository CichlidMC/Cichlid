package io.github.cichlidmc.cichlid.impl.transformer.lookup;

import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformer;
import org.jetbrains.annotations.Nullable;

public final class ClassLookups {
	public final ClassLookup preRemap;
	public final ClassLookup postTransform;

	public ClassLookups(CichlidTransformer transformer, @Nullable ClassLoader loader) {
		this.preRemap = new ClassLookup(transformer, loader, true);
		this.postTransform = new ClassLookup(transformer, loader, false);
	}

	public void put(byte[] bytes) {
		this.preRemap.put(bytes);
		this.postTransform.put(bytes);
	}
}
