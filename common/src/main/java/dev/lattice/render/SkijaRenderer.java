package dev.lattice.render;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.EncodedImageFormat;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Surface;
import io.github.humbleui.skija.impl.Library;
import io.github.humbleui.types.RRect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal Skija-backed 2D renderer.
 *
 * <p>Skija is a native Skia binding. The native library is loaded lazily on
 * the client only; a missing native binary disables the renderer instead of
 * crashing the game. The first integration exposes a small blur/shadow demo
 * surface; Minecraft GUI integration can be layered on top later.</p>
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
     * Smoke-test surface used to verify blur + drop shadow without opening a
     * Minecraft screen. It is intentionally not called during normal startup.
     */
    public static byte[] renderPreviewPng(int width, int height) {
        if (!available) {
            throw new IllegalStateException("Skija is not available", failure);
        }
        try (Surface surface = Surface.makeRasterN32Premul(width, height)) {
            Canvas canvas = surface.getCanvas();
            canvas.clear(0xFF101018);

            try (Paint background = new Paint().setColor(0xFF202838);
                 Paint shadow = new Paint().setColor(0xFF70C0FF)
                         .setImageFilter(ImageFilter.makeDropShadow(0, 8, 18, 0, 0xAA000000))) {
                RRect card = RRect.makeXYWH(40, 40, width - 80, height - 80, 24);
                canvas.drawRRect(card, background);
                canvas.drawRRect(card, shadow);
            }

            try (Paint blur = new Paint().setColor(0xFF80D0FF)
                    .setImageFilter(ImageFilter.makeBlur(12, 12, FilterTileMode.DECAL))) {
                canvas.drawCircle(width / 2.0f, height / 2.0f, 55, blur);
            }

            return surface.makeImageSnapshot()
                    .encodeToData(EncodedImageFormat.PNG)
                    .getBytes();
        }
    }
}
