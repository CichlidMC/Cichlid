package fish.cichlidmc.test_mod;

import fish.cichlidmc.cichlid.api.loaded.Mod;
import fish.cichlidmc.cichlid.api.mod.entrypoint.PreLaunchEntrypoint;

public class TestPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void preLaunch(Mod mod) {
		System.out.println("Test mod pre-launch!!!!");
		// this should succeed
		//Dummy.load();
	}
}
