package net.bzethmayr.gigantspinosaurus.usage.pipelines;

import net.bzethmayr.gigantspinosaurus.model.media.ColorSpaceReduction;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.bzethmayr.gigantspinosaurus.usage.*;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import net.bzethmayr.gigantspinosaurus.usage.vk.VulkanRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.*;
import static org.junit.jupiter.api.Assertions.*;

class VideoMarringPipelineTest {

    private static final class CpuReduction implements ReducesMedia {
        private final ColorSpaceReduction inner;
        private final ReductionStep[] steps;

        CpuReduction(final int width, final int height) {
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

    private static final Path CROSS_FORMAT_DIR = Path.of("src/test/resources/cross-format");

    private static Stream<Path> losslessPngs() throws IOException {
        return Files.list(CROSS_FORMAT_DIR)
                .filter(p -> p.getFileName().toString().endsWith("_lossless.png"))
                .sorted();
    }

    @Test
    void threeFrames_identicalContent_doesNotBreak() throws Exception {
        final Path first = losslessPngs().findFirst().orElseThrow();
        final Raster raster = CrossFormatDecoder.decode(first);
        final var reduction = new CpuReduction(raster.width(), raster.height());

        final var embedder = new MarkEmbedder();
        final var pipeline = new BindsMediaPipeline(reduction, embedder, embedder);
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

    @Test
    void threeFrames_nonIdenticalContent_doesNotBreak() throws Exception {
        final var images = losslessPngs().toList();
        if (images.size() < 3) return;

        final var rasters = images.stream()
                .map(p -> { try { return CrossFormatDecoder.decode(p); }
                            catch (IOException e) { throw new RuntimeException(e); }})
                .limit(3)
                .toList();

        final int w = rasters.stream().mapToInt(Raster::width).max().orElseThrow();
        final int h = rasters.stream().mapToInt(Raster::height).max().orElseThrow();
        final var reduction = new CpuReduction(w, h);

        final var embedder = new MarkEmbedder();
        final var pipeline = new BindsMediaPipeline(reduction, embedder, embedder);
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
        final Path first = losslessPngs().findFirst().orElseThrow();
        final Raster raster = CrossFormatDecoder.decode(first);

        try (var gpu = new VulkanRoot();
             var reduction = new VideoReduction(gpu, raster.width(), raster.height())) {

            final var embedder = new MarkEmbedder();
            final var pipeline = new BindsMediaPipeline(reduction, embedder, embedder);
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
        final var images = losslessPngs().toList();
        if (images.size() < 3) return;

        final var rasters = images.stream()
                .map(p -> { try { return CrossFormatDecoder.decode(p); }
                            catch (IOException e) { throw new RuntimeException(e); }})
                .limit(3)
                .toList();

        final int w = rasters.stream().mapToInt(Raster::width).max().orElseThrow();
        final int h = rasters.stream().mapToInt(Raster::height).max().orElseThrow();

        try (var gpu = new VulkanRoot();
             var reduction = new VideoReduction(gpu, w, h)) {

            final var embedder = new MarkEmbedder();
            final var pipeline = new BindsMediaPipeline(reduction, embedder, embedder);
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
}
