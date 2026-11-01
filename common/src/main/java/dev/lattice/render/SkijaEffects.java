package dev.lattice.render;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.SaveLayerRec;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

/**
 * Shadow and blur effects for the Lattice Skija surface.
 */
public final class SkijaEffects {
    private SkijaEffects() {
    }

    /**
     * Draws only the drop shadow of a rounded rectangle. Call this before the
     * actual card so the card covers the center of the shadow.
     */
    public static void dropShadow(double x, double y, double width, double height,
                                  double radius, float dx, float dy, float sigma, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null || sigma < 0) {
            return;
        }
        RRect rounded = SkijaShapes.roundRect(x, y, width, height, radius);
        try (ImageFilter filter = ImageFilter.makeDropShadowOnly(dx, dy, sigma, sigma, argb);
             Paint paint = new Paint().setAntiAlias(true).setImageFilter(filter)) {
            canvas.drawRRect(rounded, paint);
        }
    }

    /** Draws a blurred rounded rectangle as a glow/soft shape. */
    public static void blurredRoundedRect(double x, double y, double width, double height,
                                          double radius, float sigma, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null || sigma <= 0) {
            return;
        }
        try (ImageFilter filter = ImageFilter.makeBlur(sigma, sigma, FilterTileMode.DECAL);
             Paint paint = new Paint().setAntiAlias(true).setImageFilter(filter)
                     .setColor(argb)) {
            canvas.drawRRect(SkijaShapes.roundRect(x, y, width, height, radius), paint);
        }
    }

    public static void blurredCircle(double centerX, double centerY, double radius,
                                     float sigma, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null || sigma <= 0 || radius <= 0) {
            return;
        }
        try (ImageFilter filter = ImageFilter.makeBlur(sigma, sigma, FilterTileMode.DECAL);
             Paint paint = new Paint().setAntiAlias(true).setImageFilter(filter)
                     .setColor(argb)) {
            canvas.drawCircle((float) centerX, (float) centerY, (float) radius, paint);
        }
    }

    /**
     * Blurs the already-drawn backdrop inside a rounded rectangle.
     *
     * <p>This uses a saveLayer with a backdrop filter, so it blurs content
     * already present in the current Skija frame. It does not capture the
     * Minecraft framebuffer; the per-version host is responsible for feeding
     * that backdrop in.</p>
     */
    public static void backdropBlurRounded(double x, double y, double width, double height,
                                           double radius, float sigma, float alpha) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null || sigma <= 0 || alpha <= 0.001f || width <= 0 || height <= 0) {
            return;
        }
        Rect bounds = Rect.makeXYWH((float) x, (float) y, (float) width, (float) height);
        RRect rounded = SkijaShapes.roundRect(x, y, width, height, radius);
        int depth = canvas.save();
        try {
            canvas.clipRRect(rounded, true);
            try (ImageFilter blur = ImageFilter.makeBlur(sigma, sigma, FilterTileMode.CLAMP);
                 Paint paint = new Paint().setAlphaf(Math.min(1.0f, alpha))) {
                canvas.restoreToCount(canvas.saveLayer(new SaveLayerRec(bounds, paint, blur)));
            }
        } finally {
            canvas.restoreToCount(depth);
        }
    }
}
