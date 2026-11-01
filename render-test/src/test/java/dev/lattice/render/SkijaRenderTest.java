package dev.lattice.render;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkijaRenderTest {
    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int BLUE = 0xFF0000FF;
    private static final int WHITE = 0xFFFFFFFF;

    @BeforeAll
    static void initializeSkija() {
        SkijaRenderer.initialize();
    }

    @BeforeEach
    void requireNativeLibrary() {
        Assumptions.assumeTrue(
                SkijaRenderer.isAvailable(),
                () -> "Skija native library unavailable: " + SkijaRenderer.failure()
        );
    }

    @Test
    void rendersFilledRect() throws Exception {
        BufferedImage image = render(64, 64, frame ->
                SkijaShapes.rect(0, 0, 64, 64, RED));

        assertEquals(RED, image.getRGB(32, 32));
        assertEquals(RED, image.getRGB(2, 2));
    }

    @Test
    void rendersRoundedRectWithTransparentCorner() throws Exception {
        BufferedImage image = render(64, 64, frame ->
                SkijaShapes.roundedRect(0, 0, 64, 64, 16, GREEN));

        assertEquals(GREEN, image.getRGB(32, 32));
        assertEquals(0, alpha(image.getRGB(1, 1)));
        assertTrue(alpha(image.getRGB(32, 2)) > 0);
    }

    @Test
    void rendersOutlineWithoutFillingCenter() throws Exception {
        BufferedImage image = render(64, 64, frame ->
                SkijaShapes.roundedOutline(8, 8, 48, 48, 8, 2, RED));

        assertEquals(0, alpha(image.getRGB(32, 32)));
        assertTrue(hasOpaquePixel(image, 32, 8, 32, 12));
    }

    @Test
    void rendersDropShadowOutsideCard() throws Exception {
        BufferedImage image = render(64, 64, frame -> {
            SkijaEffects.dropShadow(16, 16, 32, 32, 8, 0, 6, 6, 0xCC000000);
            SkijaShapes.roundedRect(16, 16, 32, 32, 8, WHITE);
        });

        assertEquals(WHITE, image.getRGB(32, 32));
        assertTrue(hasOpaquePixel(image, 12, 49, 52, 56));
    }

    @Test
    void rendersBlurredShape() throws Exception {
        BufferedImage image = render(64, 64, frame ->
                SkijaEffects.blurredCircle(32, 32, 12, 6, RED));

        assertTrue(alpha(image.getRGB(32, 32)) > 0);
        assertEquals(0, alpha(image.getRGB(1, 1)));
        assertTrue(alpha(image.getRGB(32, 20)) > 0);
    }

    @Test
    void rendersHorizontalGradient() throws Exception {
        BufferedImage image = render(64, 64, frame ->
                SkijaShapes.linearGradientRect(0, 0, 64, 64, RED, BLUE, true));

        int left = image.getRGB(4, 32);
        int right = image.getRGB(60, 32);
        assertTrue(red(left) > red(right));
        assertTrue(blue(right) > blue(left));
    }

    @Test
    void appliesTransformStack() throws Exception {
        BufferedImage image = render(64, 64, frame -> {
            SkijaGfx.push();
            SkijaGfx.translate(8, 8);
            SkijaShapes.rect(0, 0, 16, 16, WHITE);
            SkijaGfx.pop();
        });

        assertEquals(WHITE, image.getRGB(16, 16));
        assertEquals(0, alpha(image.getRGB(2, 2)));
    }

    @Test
    void backdropBlurRunsWithoutThrowingAndKeepsOutput() throws Exception {
        BufferedImage image = render(64, 64, frame -> {
            SkijaShapes.rect(0, 0, 64, 64, RED);
            SkijaEffects.backdropBlurRounded(8, 8, 48, 48, 12, 8, 0.8f);
        });

        assertTrue(alpha(image.getRGB(32, 32)) > 0);
        assertNotEquals(0, image.getRGB(2, 2));
    }

    private static BufferedImage render(int width, int height, Consumer<SkijaFrame> draw)
            throws Exception {
        byte[] png;
        try (SkijaFrame frame = SkijaRenderer.beginFrame(width, height)) {
            draw.accept(frame);
            png = frame.encodePng();
        }
        assertTrue(png.length > 0);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertFalse(image == null);
        return image;
    }

    private static int alpha(int argb) {
        return (argb >>> 24) & 0xFF;
    }

    private static int red(int argb) {
        return (argb >>> 16) & 0xFF;
    }

    private static int blue(int argb) {
        return argb & 0xFF;
    }

    private static boolean hasOpaquePixel(BufferedImage image, int x0, int y0, int x1, int y1) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (alpha(image.getRGB(x, y)) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
