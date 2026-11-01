package dev.lattice.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lattice.render.SkijaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns the Skija preview PNG into a Minecraft dynamic texture.
 *
 * <p>This is the bridge used by the in-game render test screen. It intentionally
 * stays offscreen: Skija renders into its own raster surface, then the PNG is
 * uploaded as a normal Minecraft texture.</p>
 */
public final class LatticeTestTexture {
    private static final Logger LOG = LoggerFactory.getLogger("Lattice/RenderTest");
    private static final Identifier ID = Identifier.fromNamespaceAndPath("lattice", "render_test");

    private static Identifier textureId;
    private static boolean failed;

    private LatticeTestTexture() {
    }

    public static Identifier ensure() {
        if (textureId != null || failed) {
            return textureId;
        }
        if (!SkijaRenderer.isAvailable()) {
            return null;
        }
        try {
            byte[] png = SkijaRenderer.renderPreviewPng(480, 240);
            NativeImage image = NativeImage.read(png);
            DynamicTexture texture = new DynamicTexture(() -> "lattice-render-test", image);
            Minecraft.getInstance().getTextureManager().register(ID, texture);
            textureId = ID;
            LOG.info("[lattice] render test texture uploaded");
        } catch (Throwable throwable) {
            failed = true;
            LOG.warn("[lattice] failed to create render test texture", throwable);
        }
        return textureId;
    }
}
