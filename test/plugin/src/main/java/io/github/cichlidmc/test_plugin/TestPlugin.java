package io.github.cichlidmc.test_plugin;

import io.github.cichlidmc.cichlid.api.plugin.CichlidPlugin;
import io.github.cichlidmc.cichlid.api.plugin.mod.LoadableMod;

import java.nio.file.Path;

public class TestPlugin implements CichlidPlugin {
	@Override
	public LoadableMod loadMod(Path path) {
		System.out.println("Test plugin trying to load mod at " + path);
		return null;
	}
}
