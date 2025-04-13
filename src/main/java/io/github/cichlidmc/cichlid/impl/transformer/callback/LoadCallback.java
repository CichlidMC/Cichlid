package io.github.cichlidmc.cichlid.impl.transformer.callback;

@FunctionalInterface
public interface LoadCallback {
	void onLoad(byte[] bytes);
}
