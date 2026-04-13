package fish.cichlidmc.cichlid.test;

import fish.cichlidmc.cichlid.api.Cichlid;
import org.junit.jupiter.api.Test;

public final class MiscTests {
	@Test
	public void checkModuleVersion() {
		Cichlid.class.getModule().getDescriptor().version().orElseThrow();
	}
}
