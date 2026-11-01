package dev.lattice.v12111;

/**
 * Version-specific adapter for Minecraft 1.21.11.
 *
 * <p>Keep this class free of Minecraft types until the per-loader jars are
 * merged. The 1.21.11 Fabric artifact is remapped to intermediary while the
 * NeoForge artifact uses Mojang names, so shared classes with Minecraft
 * references must be package-isolated at merge time.</p>
 */
public final class MinecraftVersionAdapter {
    public static final String MINECRAFT_VERSION = "1.21.11";
    public static final String ADAPTER_ID = "mc-1.21.11";

    private MinecraftVersionAdapter() {
    }
}
