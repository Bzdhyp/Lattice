package dev.lattice.render;

import io.github.humbleui.skija.impl.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skija-backed 2D renderer entry point.
 *
 * <p>Skija is a native Skia binding. The native library is loaded lazily on
 * the client only; a missing native binary disables the renderer instead of
 * crashing the game.</p>
 */
public final class SkijaRenderer {
    private static final Logger LOG = LoggerFactory.getLogger("Lattice/Skija");

    private static boolean initialized;
    private static boolean available;
    private static Throwable failure;

    private SkijaRenderer() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Library.load();
            available = true;
            LOG.info("[lattice] Skija native renderer loaded");
        } catch (Throwable throwable) {
            failure = throwable;
            LOG.warn("[lattice] Skija native library unavailable; 2D effects disabled", throwable);
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static Throwable failure() {
        return failure;
    }

    /**
     * Opens an offscreen Skija frame. Use {@link SkijaFrame#close()} when done.
     */
    public static SkijaFrame beginFrame(int width, int height) {
        initialize();
        if (!available) {
            throw new IllegalStateException("Skija is not available", failure);
        }
        return new SkijaFrame(width, height);
    }

    /**
     * Smoke-test image used to verify rect, rounded rect, drop shadow and blur
     * without opening a Minecraft screen.
     */
    public static byte[] renderPreviewPng(int width, int height) {
        try (SkijaFrame frame = beginFrame(width, height)) {
            SkijaShapes.rect(0, 0, width, height, 0xFF101018);
            SkijaEffects.dropShadow(40, 40, width - 80, height - 80, 24,
                    0, 8, 18, 0xAA000000);
            SkijaShapes.roundedRect(40, 40, width - 80, height - 80, 24, 0xFF202838);
            SkijaShapes.roundedOutline(40, 40, width - 80, height - 80, 24, 2, 0xFF70C0FF);
            SkijaEffects.blurredCircle(width / 2.0f, height / 2.0f, 55, 12, 0xFF80D0FF);
            return frame.encodePng();
        }
    }
}
