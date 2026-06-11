package net.bzethmayr.gigantspinosaurus.usage.images;

import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.zethmayr.fungu.test.MatcherFactory.has;
import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CrossFormatDecoderTest implements TestsWithImages {

    static Matcher<Raster> looksLikeReferenceImage(final Path imageFile) {
        final String fileName = imageFile.getFileName().toString();
        return allOf(
                describedAs("an expected raster width for %0",
                        has(Raster::width, oneOf(REF_LARGE_DIM, REF_SMALL_DIM)), fileName),
                describedAs("an expected raster height for %0",
                        has(Raster::height, oneOf(REF_LARGE_DIM, REF_SMALL_DIM)), fileName),
                describedAs("no length mismatch for %0",
                        has(r -> (r.width() * r.height() * 4L) - r.rgb().length, equalTo(0L)), fileName),
                hasSomeNonZeroes(imageFile)
        );
    }

    @ParameterizedTest
    @MethodSource("sampledImageFiles")
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
