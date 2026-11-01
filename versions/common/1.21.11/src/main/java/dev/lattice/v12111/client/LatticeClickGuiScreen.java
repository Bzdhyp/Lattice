package dev.lattice.v12111.client;

import dev.lattice.client.ClickGuiTexture;
import dev.lattice.render.ClickGuiRenderer;
import dev.lattice.render.ClickGuiState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Interactive in-game ClickGUI test for the Skija 2D API.
 */
public final class LatticeClickGuiScreen extends Screen {
    private final ClickGuiState state = new ClickGuiState();
    private int panelX;
    private int panelY;

    public LatticeClickGuiScreen() {
        super(Component.literal("Lattice ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        panelX = (width - ClickGuiRenderer.LOGICAL_WIDTH) / 2;
        panelY = (height - ClickGuiRenderer.LOGICAL_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int localX = mouseX - panelX;
        int localY = mouseY - panelY;
        Identifier texture = ClickGuiTexture.update(state, localX, localY);
        if (texture != null) {
            graphics.blit(texture, panelX, panelY, 0, 0,
                    ClickGuiRenderer.LOGICAL_WIDTH, ClickGuiRenderer.LOGICAL_HEIGHT,
                    ClickGuiRenderer.LOGICAL_WIDTH * ClickGuiRenderer.SCALE,
                    ClickGuiRenderer.LOGICAL_HEIGHT * ClickGuiRenderer.SCALE);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = (int) event.x() - panelX;
        int y = (int) event.y() - panelY;

        if (x >= 270 && x <= 288 && y >= 12 && y <= 30) {
            onClose();
            return true;
        }

        for (int i = 0; i < ClickGuiState.TAB_COUNT; i++) {
            int tabY = 52 + i * 30;
            if (x >= 12 && x <= 90 && y >= tabY && y <= tabY + 24) {
                state.setTab(i);
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {
            int toggleY = 84 + i * 30;
            if (x >= 110 && x <= 284 && y >= toggleY - 12 && y <= toggleY + 12) {
                state.toggle(i);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
