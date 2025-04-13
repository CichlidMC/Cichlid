package io.github.cichlidmc.cichlid.impl.transformer.callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class LoadCallbackManager {
	private final Map<String, Callbacks> bootCallbacks = new HashMap<>();
	private final Map<ClassLoader, Map<String, Callbacks>> callbacks = new HashMap<>();

	public synchronized CompletableFuture<LoadResult> register(String name, ClassLoader loader) {
		CompletableFuture<LoadResult> future = new CompletableFuture<>();
		Callbacks callbacks = this.get(loader).computeIfAbsent(name, $ -> new Callbacks());
		callbacks.register(future);
		return future;
	}

	public synchronized void load(String name, ClassLoader loader, byte[] bytes) {
		Callbacks callbacks = this.get(loader).get(name);
		if (callbacks != null) {
			callbacks.execute(bytes);
		}
	}

	private Map<String, Callbacks> get(ClassLoader loader) {
		return loader == null ? this.bootCallbacks : this.callbacks.computeIfAbsent(loader, $ -> new HashMap<>());
	}

	private static class Callbacks {
		private final List<CompletableFuture<LoadResult>> futures = new ArrayList<>();
		private byte[] bytes;

		public void execute(byte[] bytes) {
			this.bytes = bytes;
			CompletableFuture<?>[] receivedFutures = new CompletableFuture[this.futures.size()];
			for (int i = 0; i < this.futures.size(); i++) {
				CompletableFuture<LoadResult> future = this.futures.get(i);
				LoadResult result = new LoadResult(bytes);
				receivedFutures[i] = result.receivedFuture;
				future.complete(result);
			}

			try {
				CompletableFuture.allOf(receivedFutures).get();
			} catch (ExecutionException | InterruptedException ignored) {}
		}

		public void register(CompletableFuture<LoadResult> future) {
			if (this.bytes == null) {
				this.futures.add(future);
			} else {
				// already completed, return bytes
				LoadResult result = new LoadResult(this.bytes);
				future.complete(result);
			}
		}
	}
}
