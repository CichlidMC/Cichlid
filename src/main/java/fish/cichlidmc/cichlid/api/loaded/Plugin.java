package fish.cichlidmc.cichlid.api.loaded;

import fish.cichlidmc.cichlid.impl.loaded.PluginImpl;

/// A plugin that has been loaded by Cichlid.
public sealed interface Plugin extends Loadable permits PluginImpl {
}
