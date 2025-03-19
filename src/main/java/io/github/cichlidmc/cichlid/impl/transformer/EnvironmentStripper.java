package io.github.cichlidmc.cichlid.impl.transformer;

import io.github.cichlidmc.cichlid.api.Cichlid;
import io.github.cichlidmc.cichlid.api.dist.ClientOnly;
import io.github.cichlidmc.cichlid.api.dist.DedicatedServerOnly;
import io.github.cichlidmc.cichlid.api.dist.Distribution;
import io.github.cichlidmc.cichlid.api.transformer.CichlidTransformer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.function.Function;

public enum EnvironmentStripper implements CichlidTransformer {
	INSTANCE;

	public static final String CLIENT_ONLY_DESC = Type.getDescriptor(ClientOnly.class);
	public static final String SERVER_ONLY_DESC = Type.getDescriptor(DedicatedServerOnly.class);

	private static final Distribution dist = Cichlid.distribution();

	@Override
	public boolean transform(ClassNode node) {
		Distribution classEnv = extractEnvironment(node.visibleAnnotations);
		if (classEnv != null && classEnv != dist) {
			throw new IllegalStateException("Cannot load class exclusive to '" + classEnv + "' in current environment: " + dist);
		}

		boolean filtered = false;
		filtered |= filter(node.fields, field -> field.visibleAnnotations);
		filtered |= filter(node.methods, method -> method.visibleAnnotations);
		return filtered;
	}

	private static <T> boolean filter(List<T> list, Function<T, @Nullable List<AnnotationNode>> function) {
		return list.removeIf(member -> {
			List<AnnotationNode> annotations = function.apply(member);
			Distribution dist = extractEnvironment(annotations);
			return dist != null && dist != EnvironmentStripper.dist;
		});
	}

	private static Distribution extractEnvironment(@Nullable List<AnnotationNode> annotations) {
		if (annotations == null || annotations.isEmpty())
			return null;

		for (AnnotationNode annotation : annotations) {
			if (CLIENT_ONLY_DESC.equals(annotation.desc)) {
				return Distribution.CLIENT;
			} else if (SERVER_ONLY_DESC.equals(annotation.desc)) {
				return Distribution.DEDICATED_SERVER;
			}
		}

		return null;
	}
}
