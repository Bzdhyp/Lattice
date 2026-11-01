package dev.lattice.render;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.EncodedImageFormat;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Surface;

/**
 * A single Skija raster frame. Draw through {@link SkijaShapes} / {@link SkijaEffects}
 * while this frame is open, then call {@link #encodePng()} or close it.
 */
public final class SkijaFrame implements AutoCloseable {
    private final Surface surface;
    private final Canvas canvas;
    private final int width;
    private final int height;
    private boolean closed;

    SkijaFrame(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Skija frame size must be positive");
        }
        this.width = width;
        this.height = height;
        this.surface = Surface.makeRasterN32Premul(width, height);
        this.canvas = surface.getCanvas();
        this.canvas.clear(0x00000000);
        SkijaGfx.attach(this);
    }

    public Canvas canvas() {
        return canvas;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public byte[] encodePng() {
        if (closed) {
            throw new IllegalStateException("Skija frame is closed");
        }
        try (Image image = surface.makeImageSnapshot();
             Data data = image.encodeToData(EncodedImageFormat.PNG)) {
            return data.getBytes();
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        SkijaGfx.detach(this);
        surface.close();
    }
}
