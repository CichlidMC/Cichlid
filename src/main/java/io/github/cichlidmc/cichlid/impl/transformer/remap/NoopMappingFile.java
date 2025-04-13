package io.github.cichlidmc.cichlid.impl.transformer.remap;

import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IRenamer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public enum NoopMappingFile implements IMappingFile {
	INSTANCE;

	@Override
	public Collection<? extends IPackage> getPackages() {
		return Collections.emptyList();
	}

	@Override
	public IPackage getPackage(String original) {
		return null;
	}

	@Override
	public Collection<? extends IClass> getClasses() {
		return Collections.emptyList();
	}

	@Override
	public IClass getClass(String original) {
		return null;
	}

	@Override
	public String remapPackage(String pkg) {
		return pkg;
	}

	@Override
	public String remapClass(String desc) {
		return desc;
	}

	@Override
	public String remapDescriptor(String desc) {
		return desc;
	}

	@Override
	public void write(Path path, Format format, boolean reversed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMappingFile reverse() {
		return this;
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
