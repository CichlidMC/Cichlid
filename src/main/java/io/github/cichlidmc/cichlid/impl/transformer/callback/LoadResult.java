package io.github.cichlidmc.cichlid.impl.transformer.callback;

import java.util.concurrent.CompletableFuture;

public final class LoadResult {
	public final byte[] bytes;
	public final CompletableFuture<Void> receivedFuture;

	public LoadResult(byte[] bytes) {
		this.bytes = bytes;
		this.receivedFuture = new CompletableFuture<>();
	}
}
