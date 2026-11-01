package dev.lattice.render;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

/**
 * Shape helpers for the Lattice Skija surface.
 *
 * <p>Coordinates are GUI/design pixels in a top-left origin frame. All methods
 * silently no-op when no {@link SkijaFrame} is active, which makes them safe to
 * call from optional render hooks.</p>
 */
public final class SkijaShapes {
    private SkijaShapes() {
    }

    public static RRect roundRect(double x, double y, double width, double height, double radius) {
        double w = width;
        double h = height;
        double left = x;
        double top = y;
        if (w < 0) {
            left += w;
            w = -w;
        }
        if (h < 0) {
            top += h;
            h = -h;
        }
        return RRect.makeXYWH((float) left, (float) top, (float) w, (float) h,
                (float) Math.max(radius, 0.0));
    }

    public static void rect(double x, double y, double width, double height, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null) {
            return;
        }
        double left = x;
        double top = y;
        double right = x + width;
        double bottom = y + height;
        if (left > right) {
            double swap = left;
            left = right;
            right = swap;
        }
        if (top > bottom) {
            double swap = top;
            top = bottom;
            bottom = swap;
        }
        canvas.drawRect(Rect.makeLTRB((float) left, (float) top, (float) right, (float) bottom),
                SkijaGfx.fill(argb));
    }

    public static void roundedRect(double x, double y, double width, double height,
                                   double radius, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas != null) {
            canvas.drawRRect(roundRect(x, y, width, height, radius), SkijaGfx.fill(argb));
        }
    }

    public static void roundedOutline(double x, double y, double width, double height,
                                      double radius, double thickness, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas != null) {
            canvas.drawRRect(roundRect(x, y, width, height, radius),
                    SkijaGfx.stroke(argb, thickness));
        }
    }

    public static void circle(double centerX, double centerY, double radius, int argb) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas != null && radius > 0) {
            canvas.drawCircle((float) centerX, (float) centerY, (float) radius,
                    SkijaGfx.fill(argb));
        }
    }

    public static void linearGradientRect(double x, double y, double width, double height,
                                          int startArgb, int endArgb, boolean horizontal) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null) {
            return;
        }
        float x0 = (float) x;
        float y0 = (float) y;
        float x1 = horizontal ? (float) (x + width) : x0;
        float y1 = horizontal ? y0 : (float) (y + height);
        try (Shader shader = Shader.makeLinearGradient(x0, y0, x1, y1,
                new int[]{startArgb, endArgb})) {
            Paint paint = SkijaGfx.fillShader(shader);
            canvas.drawRect(Rect.makeXYWH((float) x, (float) y, (float) width, (float) height),
                    paint);
            paint.setShader(null);
        }
    }

    public static void roundedLinearGradient(double x, double y, double width, double height,
                                             double radius, int startArgb, int endArgb,
                                             boolean horizontal) {
        Canvas canvas = SkijaGfx.canvas();
        if (canvas == null) {
            return;
        }
        float x0 = (float) x;
        float y0 = (float) y;
        float x1 = horizontal ? (float) (x + width) : x0;
        float y1 = horizontal ? y0 : (float) (y + height);
        try (Shader shader = Shader.makeLinearGradient(x0, y0, x1, y1,
                new int[]{startArgb, endArgb})) {
            Paint paint = SkijaGfx.fillShader(shader);
            canvas.drawRRect(roundRect(x, y, width, height, radius), paint);
            paint.setShader(null);
        }
    }
}
