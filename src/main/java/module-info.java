import org.jspecify.annotations.NullMarked;

@NullMarked
open module fish.cichlidmc.cichlid {
	requires static transitive org.jspecify;
	requires static transitive org.jetbrains.annotations;
	requires static org.apache.logging.log4j;

	requires java.instrument;

	requires transitive fish.cichlidmc.tinyjson;
	requires transitive fish.cichlidmc.tinycodecs;
	requires transitive fish.cichlidmc.sushi;

	exports fish.cichlidmc.cichlid.api;

	exports fish.cichlidmc.cichlid.api.dist;
	exports fish.cichlidmc.cichlid.api.loaded;

	exports fish.cichlidmc.cichlid.api.metadata;
	exports fish.cichlidmc.cichlid.api.metadata.component;

	exports fish.cichlidmc.cichlid.api.mod.entrypoint;

	exports fish.cichlidmc.cichlid.api.plugin;
	exports fish.cichlidmc.cichlid.api.plugin.mod;

	exports fish.cichlidmc.cichlid.api.version;

	// export some internals to tests
	exports fish.cichlidmc.cichlid.impl.util to fish.cichlidmc.cichlid.test;
	exports fish.cichlidmc.cichlid.impl.version to fish.cichlidmc.cichlid.test;
	exports fish.cichlidmc.cichlid.impl.version.parser to fish.cichlidmc.cichlid.test;
	exports fish.cichlidmc.cichlid.impl.version.parser.token to fish.cichlidmc.cichlid.test;
}
