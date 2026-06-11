package net.bzethmayr.gigantspinosaurus.usage.pipelines;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.usage.*;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import net.bzethmayr.gigantspinosaurus.usage.images.TestsWithImages;
import net.bzethmayr.gigantspinosaurus.usage.vk.VulkanRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class VideoMarringPipelineTest implements TestsWithImages {

    private Path firstLosslessPng() {
        return TestsWithImages.losslessPngs().findFirst().orElseThrow();
    }

    @Test
    void threeFrames_identicalContent_doesNotBreak() throws Exception {
        final Path first = firstLosslessPng();
        final Raster raster = CrossFormatDecoder.decode(first);
        final var reduction = new FakeReduction(raster.width(), raster.height());

        final var preparation = new QrMarkEmbedder(raster.width(), raster.height());
        final var pipeline = new BindsMarkingPipeline(reduction, preparation, preparation);
        final var underTest = new VideoMarring(
                BindsConstructors.defaultConstructors(),
                BindsEnvironment.desktopEnvironment(),
                pipeline, 1, 1);

        final ByteBuffer frame = raster.toBuffer();
        final var media = underTest.mediaFrame();
        final var background = underTest.background();

        // Frame 0: captured by media thread, calculated by calc thread
        media.accept(frame, 0);

        final CountDownLatch calcReady = new CountDownLatch(1);
        final AtomicReference<Throwable> calcError = new AtomicReference<>();
        Thread calcThread = new Thread(() -> {
            try {
                background.calculate(); // processes frame 0, state → APPLY_MARK
                calcReady.countDown();
                background.calculate(); // parks (APPLY_MARK → waitToProceed)
            } catch (final Throwable t) {
                calcError.set(t);
            }
        });
        calcThread.start();
        calcReady.await(1, TimeUnit.SECONDS);

        // Frame 1: mark applied
        frame.rewind();
        media.accept(frame, 1);

        // Frame 2: mark applied
        frame.rewind();
        media.accept(frame, 2);

        // Verify not broken: wake the parked calc thread
        while (calcThread.getState() != Thread.State.WAITING && calcThread.isAlive()) {
            Thread.sleep(5);
        }
        calcThread.interrupt();
        calcThread.join(1000);
        assertNull(calcError.get(), "Pipeline should not be broken");
        assertFalse(calcThread.isAlive(), "Calc thread should not hang");
    }

    private List<Raster> someLandscapeRasters(final int count) {
        return TestsWithImages.losslessPngs()
                .map(CrossFormatDecoder::decode)
                .filter(r -> r.width() == REF_LARGE_DIM)
                .limit(count)
                .toList();
    }

    @Test
    void threeFrames_nonIdenticalContent_doesNotBreak() throws Exception {
        final int w = REF_LARGE_DIM;
        final int h = REF_SMALL_DIM;
        final var rasters = someLandscapeRasters(3);

        final var reduction = new FakeReduction(w, h);

        final var embedder = new QrMarkEmbedder(w, h);
        final var pipeline = new BindsMarkingPipeline(reduction, embedder, embedder);
        final var underTest = new VideoMarring(
                BindsConstructors.defaultConstructors(),
                BindsEnvironment.desktopEnvironment(),
                pipeline, 1, 1);

        final var media = underTest.mediaFrame();
        final var background = underTest.background();

        // Frame 0: captured by media thread, calculated by calc thread
        media.accept(rasters.get(0).toBuffer(), 0);

        final CountDownLatch calcReady = new CountDownLatch(1);
        final AtomicReference<Throwable> calcError = new AtomicReference<>();
        Thread calcThread = new Thread(() -> {
            try {
                background.calculate(); // processes frame 0, state → APPLY_MARK
                calcReady.countDown();
                background.calculate(); // parks (APPLY_MARK → waitToProceed)
            } catch (final Throwable t) {
                calcError.set(t);
            }
        });
        calcThread.start();
        calcReady.await(1, TimeUnit.SECONDS);

        // Frame 1: mark applied
        media.accept(rasters.get(1).toBuffer(), 1);

        // Frame 2: mark applied
        media.accept(rasters.get(2).toBuffer(), 2);

        // Verify not broken: wake the parked calc thread
        while (calcThread.getState() != Thread.State.WAITING && calcThread.isAlive()) {
            Thread.sleep(5);
        }
        calcThread.interrupt();
        calcThread.join(1000);
        assertNull(calcError.get(), "Pipeline should not be broken");
        assertFalse(calcThread.isAlive(), "Calc thread should not hang");
    }

    @Test
    void threeFrames_gpu_identicalContent_doesNotBreak() throws Exception {
        final Path first = firstLosslessPng();
        final Raster raster = CrossFormatDecoder.decode(first);

        try (var gpu = new VulkanRoot();
             var reduction = new VideoReduction(gpu, raster.width(), raster.height())) {

            final var embedder = new QrMarkEmbedder(raster.width(), raster.height());
            final var pipeline = new BindsMarkingPipeline(reduction, embedder, embedder);
            final var underTest = new VideoMarring(
                    BindsConstructors.defaultConstructors(),
                    BindsEnvironment.desktopEnvironment(),
                    pipeline, 1, 1);

            final ByteBuffer frame = raster.toBuffer();
            final var media = underTest.mediaFrame();
            final var background = underTest.background();

            media.accept(frame, 0);

            final CountDownLatch calcReady = new CountDownLatch(1);
            final AtomicReference<Throwable> calcError = new AtomicReference<>();
            Thread calcThread = new Thread(() -> {
                try {
                    background.calculate();
                    calcReady.countDown();
                    background.calculate();
                } catch (final Throwable t) {
                    calcError.set(t);
                }
            });
            calcThread.start();
            calcReady.await(5, TimeUnit.SECONDS);

            frame.rewind();
            media.accept(frame, 1);

            frame.rewind();
            media.accept(frame, 2);

            while (calcThread.getState() != Thread.State.WAITING && calcThread.isAlive()) {
                Thread.sleep(5);
            }
            calcThread.interrupt();
            calcThread.join(1000);
            assertNull(calcError.get(), "GPU pipeline should not be broken");
            assertFalse(calcThread.isAlive(), "Calc thread should not hang");
        }
    }

    @Test
    void threeFrames_gpu_nonIdenticalContent_doesNotBreak() throws Exception {
        final int w = REF_LARGE_DIM;
        final int h = REF_SMALL_DIM;
        final var rasters = someLandscapeRasters(3);

        try (var gpu = new VulkanRoot();
             var reduction = new VideoReduction(gpu, w, h)) {

            final var embedder = new QrMarkEmbedder(w, h);
            final var pipeline = new BindsMarkingPipeline(reduction, embedder, embedder);
            final var underTest = new VideoMarring(
                    BindsConstructors.defaultConstructors(),
                    BindsEnvironment.desktopEnvironment(),
                    pipeline, 1, 1);

            final var media = underTest.mediaFrame();
            final var background = underTest.background();

            media.accept(rasters.get(0).toBuffer(), 0);

            final CountDownLatch calcReady = new CountDownLatch(1);
            final AtomicReference<Throwable> calcError = new AtomicReference<>();
            Thread calcThread = new Thread(() -> {
                try {
                    background.calculate();
                    calcReady.countDown();
                    background.calculate();
                } catch (final Throwable t) {
                    calcError.set(t);
                }
            });
            calcThread.start();
            calcReady.await(5, TimeUnit.SECONDS);

            media.accept(rasters.get(1).toBuffer(), 1);
            media.accept(rasters.get(2).toBuffer(), 2);

            while (calcThread.getState() != Thread.State.WAITING && calcThread.isAlive()) {
                Thread.sleep(5);
            }
            calcThread.interrupt();
            calcThread.join(1000);
            assertNull(calcError.get(), "GPU pipeline should not be broken");
            assertFalse(calcThread.isAlive(), "Calc thread should not hang");
        }
    }

    @Test
    void generateTimResourceFrames() throws Exception {
        final Path imagePath = firstLosslessPng();
        final String baseName = imagePath.getFileName().toString().replace("_lossless.png", "");
        final Raster raster = CrossFormatDecoder.decode(imagePath);

        final var ctors = defaultConstructors();
        final var env = desktopEnvironment();
        try (var gpu = new VulkanRoot()) {
            final var reduction = new VideoReduction(gpu, raster.width(), raster.height());
            final var creation = new MarCreation(ctors, env);
            final var embedder = new QrMarkEmbedder(raster.width(), raster.height());

            final MarCreation.ReducedFrameReceiver receiver = creation.intentToRecord(reduction.reductions());
            final ByteBuffer cleanFrame = raster.toBuffer();
            final ByteBuffer reducedFrame0 = reduction.apply(cleanFrame);
            final ExposesMar mar0 = receiver.reducedFrame(reducedFrame0, 0);

            final ByteBuffer markBuffer = embedder.emptyMark(mar0.canonicalBytes().length);
            embedder.accept(mar0.canonicalBytes(), markBuffer);

            // PLUS version (even frame index)
            final ByteBuffer plusFrame = ByteBuffer.wrap(Arrays.copyOf(raster.rgb(), raster.rgb().length));
            embedder.mark(markBuffer, plusFrame, 0);
            writeRasterAsPng(plusFrame, raster.width(), raster.height(),
                    CROSS_FORMAT_DIR.resolve(baseName + "_lossless_plus.png"));

            // MINUS version (odd frame index)
            final ByteBuffer minusFrame = ByteBuffer.wrap(Arrays.copyOf(raster.rgb(), raster.rgb().length));
            embedder.mark(markBuffer, minusFrame, 1);
            writeRasterAsPng(minusFrame, raster.width(), raster.height(),
                    CROSS_FORMAT_DIR.resolve(baseName + "_lossless_minus.png"));
        }
    }

}
