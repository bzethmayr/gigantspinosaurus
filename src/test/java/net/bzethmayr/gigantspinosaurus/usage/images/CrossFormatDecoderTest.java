package net.bzethmayr.gigantspinosaurus.usage.images;

import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.zethmayr.fungu.test.MatcherFactory.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CrossFormatDecoderTest {

    private static final Path CROSS_FORMAT_DIR = Path.of("src/test/resources/cross-format");

    static Stream<Path> imageFiles() throws IOException {
        return allImages(".png", ".jpg", ".webp", ".jp2");
    }

    static Stream<Path> allImages(final String... extensions) throws IOException {
        final Predicate<Path> filter = Stream.of(extensions)
                .map(s -> (Predicate<Path>) p -> p.getFileName().toString().toLowerCase().endsWith(s))
                .reduce(s -> false, Predicate::or);
        return Files.list(CROSS_FORMAT_DIR)
                .filter(filter)
                .sorted();
    }

    static Matcher<Raster> looksLikeReferenceImage(final Path imageFile) {
        final String fileName = imageFile.getFileName().toString();
        return allOf(
                describedAs("an expected raster width for %0",
                        has(Raster::width, oneOf(3072, 2048)), fileName),
                describedAs("an expected raster height for %0",
                        has(Raster::height, oneOf(3072, 2048)), fileName),
                describedAs("no length mismatch for %s",
                        has(r -> (r.width() * r.height() * 4L) - r.rgb().length, equalTo(0L)), fileName),
                hasSomeNonZeroes(imageFile)
        );
    }

    @ParameterizedTest
    @MethodSource("imageFiles")
    void decode_producesExpectedRasterDimensions(final Path imageFile) throws IOException {

        var raster = CrossFormatDecoder.decode(imageFile);

        assertThat(raster, looksLikeReferenceImage(imageFile));
    }

    static Matcher<Raster> hasSomeNonZeroes(final Path imageFile) {
        final String fileName = imageFile.getFileName().toString();
        return describedAs("some non-zero pixel data in %0", has((Raster r) -> {
            long nonZero = 0;
            for (byte b : r.rgb()) {
                if (b != 0) nonZero++;
            }
            return nonZero;
        }, greaterThan(0L)), fileName);
    }
}
