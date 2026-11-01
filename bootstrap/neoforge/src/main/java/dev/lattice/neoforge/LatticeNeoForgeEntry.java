package dev.lattice.neoforge;

import dev.lattice.common.Lattice;
import dev.lattice.render.SkijaRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforgespi.language.IModFileInfo;

/**
 * Single NeoForge entrypoint shared by every Minecraft version.
 *
 * <p>NeoForge discovers this annotated class from the jar, then the class
 * selects the version-specific payload. No payload class is linked until its
 * Minecraft version branch is chosen.</p>
 */
@Mod(Lattice.MOD_ID)
public final class LatticeNeoForgeEntry {
    public LatticeNeoForgeEntry(IEventBus eventBus) {
        IModFileInfo minecraft = ModList.get().getModFileById("minecraft");
        if (minecraft == null) {
            throw new IllegalStateException("Minecraft mod file is missing");
        }

        invokePayload(minecraft.versionString());

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            SkijaRenderer.initialize();
        }
    }

    private static void invokePayload(String minecraft) {
        String payload;
        if (minecraft.startsWith("1.21.11")) {
            payload = "dev.lattice.v12111.neoforge.LatticeNeoForgePayload";
        } else if (minecraft.startsWith("26.3")) {
            payload = "dev.lattice.v263.neoforge.LatticeNeoForgePayload";
        } else {
            throw new IllegalStateException("Lattice has no NeoForge payload for Minecraft " + minecraft);
        }

        try {
            Class.forName(payload, true, LatticeNeoForgeEntry.class.getClassLoader())
                    .getMethod("initialize")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to initialize " + payload, exception);
        }
    }
}
