package net.bzethmayr.gigantspinosaurus.usage.images;

import net.bzethmayr.gigantspinosaurus.model.media.ColorSpaceReduction;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.zethmayr.fungu.Fork;
import net.zethmayr.fungu.ForkFactory;
import net.zethmayr.fungu.test.TestRuntimeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION;
import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;

public interface TestsWithImages {
    Path CROSS_FORMAT_DIR = Path.of("src/test/resources/cross-format");
    int SAMPLE_STRIDE = 7;
    int REF_LARGE_DIM = 3072;
    int REF_SMALL_DIM = 2048;

    final class FakeReduction implements ReducesMedia {
        private final ColorSpaceReduction inner;
        private final ReductionStep[] steps;

        public FakeReduction(final int width, final int height) {
            inner = new ColorSpaceReduction(width, height);
            steps = new ReductionStep[]{new ReductionStep(YCBCR_ID, YCBCR_VERSION)};
        }

        @Override
        public ReductionStep[] reductions() {
            return steps;
        }

        @Override
        public ByteBuffer apply(final ByteBuffer input) {
            return inner.apply(input);
        }
    }

    default void writeRasterAsPng(final ByteBuffer buf, final int width, final int height, final Path path) {
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buf.rewind();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int off = (y * width + x) * 4;
                final int r = buf.get(off) & 0xFF;
                final int g = buf.get(off + 1) & 0xFF;
                final int b = buf.get(off + 2) & 0xFF;
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        try {
            ImageIO.write(img, "png", path.toFile());
        } catch (final IOException ioe) {
            throw new TestRuntimeException(ioe);
        }
    }

    static Stream<Path> allImages(final String... extensions) {
        final Predicate<Path> filter = Stream.of(extensions)
                .map(s -> (Predicate<Path>) p ->
                        p.getFileName().toString().toLowerCase().endsWith(s))
                .reduce(s -> false, Predicate::or);
        try (Stream<Path> listing = Files.list(CROSS_FORMAT_DIR)) {
            return listing.filter(filter)
                    .sorted()
                    .toList().stream();
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    static Stream<Path> imageFiles() {
        return allImages(".png", ".jpg", ".webp", ".jp2");
    }

    static Stream<Path> sampledImageFiles(final int stride) {
        final int offset = TEST_RANDOM.nextInt(stride);
        return imageFiles().map(ForkFactory.overOrdinal())
                .filter(f -> f.bottom() % stride == offset)
                .map(Fork::top);
    }

    static Stream<Path> sampledImageFiles(){
        return sampledImageFiles(SAMPLE_STRIDE);
    }

    static Stream<Path> losslessPngs() {
        return allImages("_lossless.png");
    }

    default byte[] readAll(final ByteBuffer buf) {
        buf.rewind();
        final byte[] b = new byte[buf.remaining()];
        buf.get(b);
        return b;
    }
}
