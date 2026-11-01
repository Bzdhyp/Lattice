package dev.lattice.v263.fabric;

import dev.lattice.common.Lattice;
import dev.lattice.v263.MinecraftVersionAdapter;

public final class LatticeFabricPayload {
    public static void initialize() {
        Lattice.initialize("fabric", MinecraftVersionAdapter.MINECRAFT_VERSION,
                MinecraftVersionAdapter.ADAPTER_ID);
    }

    private LatticeFabricPayload() {
    }
}
