package net.bzethmayr.gigantspinosaurus.usage.images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class CrossFormatDecoder {

    public record Raster(int width, int height, byte[] rgb) {
        public ByteBuffer toBuffer() {
            return ByteBuffer.wrap(rgb);
        }
    }

    public static Raster decode(final Path path) throws IOException {
        final BufferedImage img = ImageIO.read(path.toFile());
        final int w = img.getWidth();
        final int h = img.getHeight();
        final byte[] rgb = new byte[w * h * 4];
        int off = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int argb = img.getRGB(x, y);
                rgb[off++] = (byte) ((argb >> 16) & 0xFF);
                rgb[off++] = (byte) ((argb >> 8) & 0xFF);
                rgb[off++] = (byte) (argb & 0xFF);
                rgb[off++] = (byte) 0;
            }
        }
        return new Raster(w, h, rgb);
    }

    private CrossFormatDecoder() {}
}
