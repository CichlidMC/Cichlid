package io.github.cichlidmc.cichlid.api.transformer;

import io.github.cichlidmc.cichlid.impl.transformer.CichlidTransformerManager;
import org.objectweb.asm.tree.ClassNode;

@FunctionalInterface
public interface CichlidTransformer {
	/**
	 * Arbitrarily transform the given class before loading it.
	 * With great power comes great responsibility.
	 * @return true if the class was modified in any way
	 */
	boolean transform(ClassNode node);

	/**
	 * Register a new transformer. The transformer will be invoked for all classes going forward.
	 */
	static void register(CichlidTransformer transformer) {
		CichlidTransformerManager.registerTransformer(transformer);
	}
}
