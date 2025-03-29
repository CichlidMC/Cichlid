package io.github.cichlidmc.test_mod;

import io.github.cichlidmc.cichlid.api.loaded.Mod;
import io.github.cichlidmc.cichlid.api.mod.entrypoint.EarlySetupEntrypoint;

public class TestEarlySetup implements EarlySetupEntrypoint {
	@Override
	public void earlySetup(Mod mod) {
		System.out.println("Test mod early setup!!!!");
		// this should fail
		// Dummy.load();
	}
}
