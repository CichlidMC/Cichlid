package io.github.cichlidmc.cichlid.impl;

import io.github.cichlidmc.cichlid.api.dist.Distribution;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CichlidArgs {
	public final String version;
	public final Distribution dist;

	public CichlidArgs(String version, Distribution dist) {
		this.version = version;
		this.dist = dist;
	}

	@Nullable
	public static CichlidArgs parse(@Nullable String args) {
		if (args == null)
			return null;

		Map<String, String> map = new HashMap<>();
		String[] split = args.split(",");
		for (String arg : split) {
			String[] splitArg = arg.split("=");
			if (splitArg.length != 2)
				return null;

			map.put(splitArg[0], splitArg[1]);
		}

		String version = map.get("version");
		if (version == null)
			return null;

		String dist = map.get("dist");
		Distribution parsed = Distribution.of(dist);
		if (parsed == null)
			return null;

		return new CichlidArgs(version, parsed);
	}
}
