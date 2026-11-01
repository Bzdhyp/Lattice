package dev.lattice.render;

/**
 * State for the in-game ClickGUI test.
 */
public final class ClickGuiState {
    public static final int TAB_RENDER = 0;
    public static final int TAB_EFFECTS = 1;
    public static final int TAB_SETTINGS = 2;
    public static final int TAB_COUNT = 3;

    private int tab;
    private boolean shadow = true;
    private boolean blur = true;
    private boolean outline = true;

    public int tab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = Math.max(0, Math.min(TAB_COUNT - 1, tab));
    }

    public boolean shadow() {
        return shadow;
    }

    public boolean blur() {
        return blur;
    }

    public boolean outline() {
        return outline;
    }

    public void toggle(int index) {
        switch (index) {
            case 0 -> shadow = !shadow;
            case 1 -> blur = !blur;
            case 2 -> outline = !outline;
            default -> {
            }
        }
    }

    public int signature() {
        return (tab << 3) | (shadow ? 1 : 0) | (blur ? 2 : 0) | (outline ? 4 : 0);
    }
}
