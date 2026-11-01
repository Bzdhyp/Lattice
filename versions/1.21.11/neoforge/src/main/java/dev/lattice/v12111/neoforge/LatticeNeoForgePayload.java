package dev.lattice.v12111.neoforge;

import dev.lattice.core.Lattice;
import dev.lattice.v12111.MinecraftVersionAdapter;

public final class LatticeNeoForgePayload {
    public static void initialize() {
        Lattice.initialize("neoforge", MinecraftVersionAdapter.MINECRAFT_VERSION,
                MinecraftVersionAdapter.ADAPTER_ID);
    }

    private LatticeNeoForgePayload() {
    }
}
