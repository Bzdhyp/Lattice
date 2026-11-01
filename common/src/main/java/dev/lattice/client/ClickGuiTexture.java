package dev.lattice.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lattice.render.ClickGuiRenderer;
import dev.lattice.render.ClickGuiState;
import dev.lattice.render.SkijaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uploads the Skija ClickGUI raster into a Minecraft dynamic texture.
 */
public final class ClickGuiTexture {
    private static final Logger LOG = LoggerFactory.getLogger("Lattice/ClickGui");
    private static final Identifier ID = Identifier.fromNamespaceAndPath("lattice", "clickgui");

    private static DynamicTexture texture;
    private static Identifier textureId;
    private static int lastSignature = Integer.MIN_VALUE;
    private static boolean failed;

    private ClickGuiTexture() {
    }

    public static Identifier update(ClickGuiState state, int mouseX, int mouseY) {
        if (!SkijaRenderer.isAvailable() || failed) {
            return textureId;
        }
        int signature = state.signature() * 31 * 31 + mouseX * 31 + mouseY;
        if (signature == lastSignature && textureId != null) {
            return textureId;
        }
        try {
            byte[] png = ClickGuiRenderer.renderPng(state, mouseX, mouseY);
            NativeImage image = NativeImage.read(png);
            if (texture == null) {
                texture = new DynamicTexture(() -> "lattice-clickgui", image);
                Minecraft.getInstance().getTextureManager().register(ID, texture);
                textureId = ID;
                LOG.info("[lattice] ClickGUI texture uploaded");
            } else {
                texture.setPixels(image);
                texture.upload();
            }
            lastSignature = signature;
        } catch (Throwable throwable) {
            failed = true;
            LOG.warn("[lattice] failed to update ClickGUI texture", throwable);
        }
        return textureId;
    }
}
