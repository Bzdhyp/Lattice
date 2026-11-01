package dev.lattice.v12111.client;

import dev.lattice.client.LatticeTestTexture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * In-game visual test for the Skija 2D API.
 */
public final class LatticeRenderTestScreen extends Screen {
    public LatticeRenderTestScreen() {
        super(Component.literal("Lattice Render Test"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Identifier texture = LatticeTestTexture.ensure();
        if (texture != null) {
            graphics.blit(texture, 8, 8, 0, 0, 240.0f, 120.0f, 480.0f, 240.0f);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
