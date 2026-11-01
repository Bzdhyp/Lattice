package dev.lattice.render;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Typeface;

/**
 * Skija-drawn ClickGUI test layout.
 *
 * <p>Logical size is 300x200; the raster surface is 600x400 (2x) for crisp
 * text and edges.</p>
 */
public final class ClickGuiRenderer {
    public static final int LOGICAL_WIDTH = 300;
    public static final int LOGICAL_HEIGHT = 200;
    public static final int SCALE = 2;

    private static final String[] TABS = {"Render", "Effects", "Settings"};
    private static final String[] TOGGLES = {"Drop shadow", "Backdrop blur", "Rounded outline"};

    private static Typeface typeface;
    private static Font titleFont;
    private static Font bodyFont;

    static {
        try {
            typeface = FontMgr.getDefault().matchFamiliesStyle(
                    new String[]{"Segoe UI", "Arial", "sans-serif"}, FontStyle.NORMAL);
            if (typeface != null) {
                titleFont = new Font(typeface, 16);
                bodyFont = new Font(typeface, 11);
            }
        } catch (Throwable ignored) {
            // Text is optional; the ClickGUI still renders without a system font.
        }
    }

    private ClickGuiRenderer() {
    }

    public static byte[] renderPng(ClickGuiState state, int mouseX, int mouseY) {
        try (SkijaFrame frame = SkijaRenderer.beginFrame(LOGICAL_WIDTH * SCALE,
                LOGICAL_HEIGHT * SCALE)) {
            frame.canvas().clear(0x00000000);
            SkijaGfx.scale(SCALE, SCALE);
            draw(state, mouseX, mouseY);
            return frame.encodePng();
        }
    }

    private static void draw(ClickGuiState state, int mouseX, int mouseY) {
        int panelX = 4;
        int panelY = 4;
        int panelW = 292;
        int panelH = 192;

        if (state.shadow()) {
            SkijaEffects.dropShadow(panelX, panelY, panelW, panelH, 16, 0, 8, 18, 0x99000000);
        }
        SkijaShapes.roundedRect(panelX, panelY, panelW, panelH, 16, 0xF0141826);
        if (state.outline()) {
            SkijaShapes.roundedOutline(panelX, panelY, panelW, panelH, 16, 1, 0x6670C0FF);
        }

        SkijaShapes.roundedRect(4, 4, 292, 34, 16, 0xCC1D2436);
        text("Lattice ClickGUI", 16, 26, titleFont, 0xFFEAF4FF);
        boolean closeHover = hover(mouseX, mouseY, 270, 12, 18, 18);
        SkijaShapes.roundedRect(270, 12, 18, 18, 6,
                closeHover ? 0xFFE05A6A : 0xFF2A3348);
        text("x", 276, 26, bodyFont, 0xFFFFFFFF);

        for (int i = 0; i < TABS.length; i++) {
            int y = 52 + i * 30;
            boolean selected = state.tab() == i;
            boolean tabHover = hover(mouseX, mouseY, 12, y, 78, 24);
            int color = selected ? 0xFF3B82F6 : tabHover ? 0xFF2A3A55 : 0xCC1A2131;
            SkijaShapes.roundedRect(12, y, 78, 24, 7, color);
            text(TABS[i], 22, y + 16, bodyFont, selected ? 0xFFFFFFFF : 0xFFB8C4DA);
        }

        SkijaShapes.roundedRect(102, 48, 186, 140, 10, 0xCC171D2B);
        text(TABS[state.tab()], 116, 68, titleFont, 0xFFEAF4FF);

        for (int i = 0; i < TOGGLES.length; i++) {
            int y = 84 + i * 30;
            boolean hover = hover(mouseX, mouseY, 110, y - 12, 170, 24);
            if (hover) {
                SkijaShapes.roundedRect(108, y - 12, 174, 24, 6, 0x332A3A55);
            }
            text(TOGGLES[i], 116, y + 4, bodyFont, 0xFFC8D4E8);
            drawSwitch(240, y - 9, isOn(state, i));
        }

        if (state.blur()) {
            SkijaEffects.blurredRoundedRect(116, 156, 150, 22, 11, 10, 0x6680D0FF);
        }
        text("Skija 2D  |  hover / click test", 116, 184, bodyFont, 0xFF6F809B);
    }

    private static void drawSwitch(int x, int y, boolean on) {
        SkijaShapes.roundedRect(x, y, 30, 16, 8,
                on ? 0xFF3B82F6 : 0xFF39445A);
        double knobX = on ? x + 19 : x + 3;
        SkijaShapes.circle(knobX, y + 8, 6, on ? 0xFFFFFFFF : 0xFF8E9AB0);
    }

    private static boolean isOn(ClickGuiState state, int index) {
        return switch (index) {
            case 0 -> state.shadow();
            case 1 -> state.blur();
            case 2 -> state.outline();
            default -> false;
        };
    }

    private static boolean hover(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static void text(String value, float x, float baseline, Font font, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null || font == null || value == null || value.isEmpty()) {
            return;
        }
        canvas.drawString(value, x, baseline, font, SkijaGfx.fill(argb));
    }
}
