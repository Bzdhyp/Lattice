package dev.lattice.v263.neoforge;

import dev.lattice.common.Lattice;
import dev.lattice.v263.MinecraftVersionAdapter;

public final class LatticeNeoForgePayload {
    public static void initialize() {
        Lattice.initialize("neoforge", MinecraftVersionAdapter.MINECRAFT_VERSION,
                MinecraftVersionAdapter.ADAPTER_ID);
    }

    private LatticeNeoForgePayload() {
    }
}
