package net.bzethmayr.gigantspinosaurus.usage.images;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CrossFormatDecoderTest {

    private static final Path CROSS_FORMAT_DIR = Path.of("src/test/resources/cross-format");

    static Stream<Path> imageFiles() throws IOException {
        return Files.list(CROSS_FORMAT_DIR)
                .filter(p -> {
                    var name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".png") || name.endsWith(".jpg")
                            || name.endsWith(".webp") || name.endsWith(".jp2");
                })
                .sorted();
    }

    @ParameterizedTest
    @MethodSource("imageFiles")
    void decode_producesExpectedRasterDimensions(final Path imageFile) throws IOException {
        var raster = CrossFormatDecoder.decode(imageFile);
        assertNotNull(raster);
        assertTrue(raster.width() > 0, "Width must be positive");
        assertTrue(raster.height() > 0, "Height must be positive");
        assertEquals(raster.width() * raster.height() * 3L, raster.rgb().length,
                () -> "Byte array length mismatch for " + imageFile.getFileName());
    }

    @Test
    void decode_losslessPng_hasNonZeroData() throws IOException {
        var png = CROSS_FORMAT_DIR.resolve("IMG0008_lossless.png");
        assertTrue(Files.exists(png), "Lossless reference image not found: " + png);
        var raster = CrossFormatDecoder.decode(png);
        assertNotNull(raster);
        long nonZero = 0;
        for (byte b : raster.rgb()) {
            if (b != 0) nonZero++;
        }
        assertTrue(nonZero > 0, "Expected non-zero pixel data in lossless PNG");
    }

    @Test
    void decode_jpeg2000_roundTrips() throws IOException {
        var jp2 = CROSS_FORMAT_DIR.resolve("IMG0008_dwt_50.jp2");
        assertTrue(Files.exists(jp2), "JPEG 2000 file not found: " + jp2);
        var raster = CrossFormatDecoder.decode(jp2);
        assertNotNull(raster);
        assertTrue(raster.width() > 0);
        assertTrue(raster.height() > 0);
    }

    @Test
    void decode_webp_roundTrips() throws IOException {
        var webp = CROSS_FORMAT_DIR.resolve("IMG0008_dct_50.webp");
        assertTrue(Files.exists(webp), "WebP file not found: " + webp);
        var raster = CrossFormatDecoder.decode(webp);
        assertNotNull(raster);
        assertTrue(raster.width() > 0);
        assertTrue(raster.height() > 0);
    }
}
