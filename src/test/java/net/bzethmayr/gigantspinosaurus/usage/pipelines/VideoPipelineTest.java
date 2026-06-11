package net.bzethmayr.gigantspinosaurus.usage.pipelines;

import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder;
import net.bzethmayr.gigantspinosaurus.usage.images.TestsWithImages;
import net.bzethmayr.gigantspinosaurus.usage.vk.VulkanRoot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.REDUCED_OUTPUT_BYTES;
import static org.junit.jupiter.api.Assertions.*;

class VideoPipelineTest implements TestsWithImages {

    static Stream<Path> losslessPngs() throws Exception {
        return TestsWithImages.allImages("_lossless.png");
    }

    @ParameterizedTest
    @MethodSource("losslessPngs")
    void pipeline_produces288Bytes_forLosslessPng(final Path each) throws Exception {
        final var raster = CrossFormatDecoder.decode(each);

        try (var root = new VulkanRoot();
             var reduction = new VideoReduction(root, raster.width(), raster.height())) {

            final var result = reduction.apply(raster.toBuffer());
            assertEquals(REDUCED_OUTPUT_BYTES, result.remaining());
        }
    }

    @Test
    void pipeline_isDeterministic_sameInputSameOutput() throws Exception {
        final Path first = losslessPngs().findFirst().orElseThrow();
        final var raster = CrossFormatDecoder.decode(first);
        final var input = raster.toBuffer();

        try (var root = new VulkanRoot();
             var reduction = new VideoReduction(root, raster.width(), raster.height())) {

            final var run1 = reduction.apply(input);
            final var run2 = reduction.apply(input);
            assertArrayEquals(
                    readAll(run1),
                    readAll(run2));
        }
    }

    @Test
    void pipeline_differentFrames_differentOutput() throws Exception {
        final var images = losslessPngs().toList();
        if (images.size() < 2) return;

        final var raster1 = CrossFormatDecoder.decode(images.get(0));
        final var raster2 = CrossFormatDecoder.decode(images.get(1));
        final int w = Math.max(raster1.width(), raster2.width());
        final int h = Math.max(raster1.height(), raster2.height());

        try (var root = new VulkanRoot();
             var reduction = new VideoReduction(root, w, h)) {

            final var result1 = reduction.apply(raster1.toBuffer());
            final var result2 = reduction.apply(raster2.toBuffer());
            assertFalse(
                    java.util.Arrays.equals(readAll(result1), readAll(result2)),
                    "Different frames should produce different reductions");
        }
    }

}
