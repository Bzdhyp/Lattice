package dev.lattice.render;

import io.github.humbleui.skija.BlendMode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.skija.PaintStrokeCap;
import io.github.humbleui.skija.PaintStrokeJoin;
import io.github.humbleui.skija.Shader;

/**
 * Shared Skija drawing state and Paint reuse.
 *
 * <p>Skija has no global color/blend state; colors live on {@link Paint}. The two
 * cached Paints are valid only for the duration of one draw call and must not be
 * retained by callers.</p>
 */
public final class SkijaGfx {
    private static SkijaFrame currentFrame;
    private static int matrixDepth;

    private static Paint fillPaint;
    private static Paint strokePaint;

    private SkijaGfx() {
    }

    static void attach(SkijaFrame frame) {
        if (currentFrame != null && currentFrame != frame) {
            throw new IllegalStateException("Another Skija frame is already active");
        }
        currentFrame = frame;
        matrixDepth = 0;
    }

    static void detach(SkijaFrame frame) {
        if (currentFrame == frame) {
            currentFrame = null;
            matrixDepth = 0;
        }
    }

    public static boolean active() {
        return currentFrame != null;
    }

    public static Canvas canvas() {
        return currentFrame == null ? null : currentFrame.canvas();
    }

    private static Paint fillPaint() {
        if (fillPaint == null) {
            fillPaint = new Paint().setAntiAlias(true);
        }
        return fillPaint;
    }

    private static Paint strokePaint() {
        if (strokePaint == null) {
            strokePaint = new Paint().setAntiAlias(true).setMode(PaintMode.STROKE);
        }
        return strokePaint;
    }

    public static Paint fill(int argb) {
        Paint paint = fillPaint();
        paint.setShader(null);
        paint.setColorFilter(null);
        paint.setMaskFilter(null);
        paint.setImageFilter(null);
        paint.setMode(PaintMode.FILL);
        paint.setColor(argb);
        paint.setBlendMode(BlendMode.SRC_OVER);
        return paint;
    }

    public static Paint fillShader(Shader shader) {
        Paint paint = fillPaint();
        paint.setColorFilter(null);
        paint.setMaskFilter(null);
        paint.setImageFilter(null);
        paint.setMode(PaintMode.FILL);
        paint.setColor(0xFFFFFFFF);
        paint.setShader(shader);
        paint.setBlendMode(BlendMode.SRC_OVER);
        return paint;
    }

    public static Paint stroke(int argb, double width) {
        Paint paint = strokePaint();
        paint.setShader(null);
        paint.setColorFilter(null);
        paint.setMaskFilter(null);
        paint.setImageFilter(null);
        paint.setMode(PaintMode.STROKE);
        paint.setStrokeWidth((float) Math.max(width, 0.0));
        paint.setStrokeCap(PaintStrokeCap.BUTT);
        paint.setStrokeJoin(PaintStrokeJoin.MITER);
        paint.setColor(argb);
        paint.setBlendMode(BlendMode.SRC_OVER);
        return paint;
    }

    public static void push() {
        Canvas canvas = canvas();
        if (canvas != null) {
            canvas.save();
            matrixDepth++;
        }
    }

    public static void pop() {
        Canvas canvas = canvas();
        if (canvas != null && matrixDepth > 0) {
            canvas.restore();
            matrixDepth--;
        }
    }

    public static void translate(double x, double y) {
        Canvas canvas = canvas();
        if (canvas != null) {
            canvas.translate((float) x, (float) y);
        }
    }

    public static void scale(double x, double y) {
        Canvas canvas = canvas();
        if (canvas != null) {
            canvas.scale((float) x, (float) y);
        }
    }

    public static void rotateZ(double degrees) {
        Canvas canvas = canvas();
        if (canvas != null) {
            canvas.rotate((float) degrees);
        }
    }

    public static int depth() {
        return matrixDepth;
    }
}
