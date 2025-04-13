package io.github.cichlidmc.cichlid.impl.transformer.remap;

import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IRenamer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public final class TrackedMappingFile implements IMappingFile {
	private final IMappingFile wrapped;
	private boolean remappedAnything;

	public TrackedMappingFile(IMappingFile wrapped) {
		this.wrapped = wrapped;
	}

	public boolean remappedAnything() {
		return this.remappedAnything;
	}

	private void mark() {
		this.remappedAnything = true;
	}

	@Override
	public IClass getClass(String original) {
		IClass clazz = this.wrapped.getClass(original);
		if (clazz != null)
			this.mark();
		return clazz;
	}

	@Override
	public String remapPackage(String pkg) {
		String remapped = this.wrapped.remapPackage(pkg);
		if (!pkg.equals(remapped))
			this.mark();
		return remapped;
	}

	@Override
	public String remapClass(String desc) {
		String clazz = this.wrapped.remapClass(desc);
		if (desc.equals(clazz))
			this.mark();
		return clazz;
	}

	// too much work

	@Override
	public Collection<? extends IPackage> getPackages() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPackage getPackage(String original) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<? extends IClass> getClasses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String remapDescriptor(String desc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(Path path, Format format, boolean reversed) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMappingFile reverse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMappingFile rename(IRenamer renamer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMappingFile chain(IMappingFile other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMappingFile merge(IMappingFile other) {
		throw new UnsupportedOperationException();
	}
}
