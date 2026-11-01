package dev.lattice.client;

import dev.lattice.render.SkijaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the in-game rendering test screen once on the client.
 */
public final class LatticeClientTest {
    private static final Logger LOG = LoggerFactory.getLogger("Lattice/RenderTest");
    private static boolean opened;

    private LatticeClientTest() {
    }

    public static void openOnce(String minecraftVersion) {
        if (opened) {
            return;
        }
        if (!Boolean.parseBoolean(System.getProperty("lattice.renderTest", "true"))) {
            return;
        }
        if (!SkijaRenderer.isAvailable()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        // Do not open during the title screen/world load; those screens replace
        // anything we set. Wait until a client world is active.
        if (client.level == null) {
            return;
        }
        opened = true;
        try {
            String className = minecraftVersion.startsWith("26.3")
                    ? "dev.lattice.v263.client.LatticeRenderTestScreen"
                    : "dev.lattice.v12111.client.LatticeRenderTestScreen";
            Screen screen = (Screen) Class.forName(className)
                    .getDeclaredConstructor()
                    .newInstance();
            client.setScreenAndShow(screen);
            LOG.info("[lattice] opening render test screen for Minecraft {}", minecraftVersion);
        } catch (Throwable throwable) {
            LOG.warn("[lattice] failed to open render test screen", throwable);
        }
    }
}
