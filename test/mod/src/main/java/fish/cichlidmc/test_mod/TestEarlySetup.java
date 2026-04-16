package fish.cichlidmc.test_mod;

import fish.cichlidmc.cichlid.api.loaded.Mod;
import fish.cichlidmc.cichlid.api.mod.entrypoint.EarlySetupEntrypoint;

public class TestEarlySetup implements EarlySetupEntrypoint {
	@Override
	public void earlySetup(Mod mod) {
		System.out.println("Test mod early setup!!!!");
		// this should fail when uncommented
		// Dummy.load();
	}
}
