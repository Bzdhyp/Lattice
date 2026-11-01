package dev.lattice.v12111.fabric;

import dev.lattice.core.Lattice;
import dev.lattice.v12111.MinecraftVersionAdapter;

public final class LatticeFabricPayload {
    public static void initialize() {
        Lattice.initialize("fabric", MinecraftVersionAdapter.MINECRAFT_VERSION,
                MinecraftVersionAdapter.ADAPTER_ID);
    }

    private LatticeFabricPayload() {
    }
}
