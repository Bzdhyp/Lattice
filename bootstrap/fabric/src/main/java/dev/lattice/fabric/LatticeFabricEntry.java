package dev.lattice.fabric;

import dev.lattice.core.Lattice;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Single Fabric entrypoint shared by every Minecraft version.
 *
 * <p>The loader only loads this class eagerly. It selects the version-specific
 * payload by name, so a combined jar never links classes from the wrong
 * Minecraft version.</p>
 */
public final class LatticeFabricEntry implements ModInitializer {
    @Override
    public void onInitialize() {
        String minecraft = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElseThrow(() -> new IllegalStateException("Minecraft mod container is missing"));

        invokePayload(minecraft);
    }

    private static void invokePayload(String minecraft) {
        String payload;
        if (minecraft.startsWith("1.21.11")) {
            payload = "dev.lattice.v12111.fabric.LatticeFabricPayload";
        } else if (minecraft.startsWith("26.3")) {
            payload = "dev.lattice.v263.fabric.LatticeFabricPayload";
        } else {
            throw new IllegalStateException("Lattice has no Fabric payload for Minecraft " + minecraft);
        }

        try {
            Class.forName(payload, true, LatticeFabricEntry.class.getClassLoader())
                    .getMethod("initialize")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to initialize " + payload, exception);
        }
    }

    static {
        // Keep the constant referenced so a future refactor cannot silently
        // drop the shared mod id from the entrypoint class.
        if (!"lattice".equals(Lattice.MOD_ID)) {
            throw new IllegalStateException("Unexpected Lattice mod id");
        }
    }
}
