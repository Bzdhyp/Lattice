package dev.lattice.v263;

/**
 * Version-specific adapter for Minecraft 26.3.
 *
 * <p>Minecraft 26.x is non-obfuscated: Fabric and NeoForge both compile
 * against official names. This class is still kept version-package-isolated
 * so a future combined artifact cannot collide with the 1.21.11 tree.</p>
 */
public final class MinecraftVersionAdapter {
    public static final String MINECRAFT_VERSION = "26.3";
    public static final String ADAPTER_ID = "mc-26.3";

    private MinecraftVersionAdapter() {
    }
}
