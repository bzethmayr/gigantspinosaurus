package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoMarringTest implements TestsModel, TestsWithBytes {
    private static final int CADENCE = 60;
    private static final int EMPTY = 10;

    private BindsMarkingPipeline pipeline;
    private VideoMarring underTest;

    @SafeVarargs
    private void setUpMockPipeline(final Consumer<BindsMarkingPipeline>... configs) {
        final ReducesMedia mockReduction = mock();
        final PreparesMark mockPreparation = mock();
        final MarksMedia mockMarking = mock();
        pipeline = new BindsMarkingPipeline(mockReduction, mockPreparation, mockMarking);
        Stream.of(configs).forEach(c -> c.accept(pipeline));
        underTest = new VideoMarring(
                BindsConstructors.defaultConstructors(), BindsEnvironment.desktopEnvironment(),
                pipeline, CADENCE, EMPTY);
    }

    private Consumer<BindsMarkingPipeline> reducerSteps(final ReductionStep... steps) {
        return p -> doReturn(steps).when(p.reducer()).reductions();
    }

    private Consumer<BindsMarkingPipeline> fakeReducer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(SOME)).when(p.reducer()).apply(any());
    }

    private Consumer<BindsMarkingPipeline> fakePreparer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(MANY)).when(p.combiner()).emptyMark(anyInt());
    }

    private Consumer<BindsMarkingPipeline> minimalFakes() {
        return reducerSteps().andThen(fakeReducer()).andThen(fakePreparer());
    }

    @Test
    void background_onMediaThread_throwsOnUse() {
        setUpMockPipeline(minimalFakes());
        final VideoMarring.MediaFrameAcceptor media = underTest.mediaFrame();
        final VideoMarring.BackgroundCalculator background = underTest.background();
        final ByteBuffer rawFrame = fakeMediaBytes(LOTS);

        assertDoesNotThrow(() -> media.accept(rawFrame, 0));
        assertThrows(IllegalArgumentException.class, background::calculate);
    }

    @Test
    void background_pipelineCrash_setsBrokenState() throws Exception {
        setUpMockPipeline(
                reducerSteps(),
                p -> doThrow(new RuntimeException("GPU device lost")).when(p.reducer()).apply(any()),
                fakePreparer());

        final var media = underTest.mediaFrame();
        final var background = underTest.background();
        final ByteBuffer rawFrame = fakeMediaBytes(LOTS);

        final CountDownLatch mediaDone = new CountDownLatch(1);
        new Thread(() -> {
            media.accept(rawFrame, 0);
            mediaDone.countDown();
        }).start();
        mediaDone.await(1, TimeUnit.SECONDS);

        final AtomicReference<Throwable> calcError = new AtomicReference<>();
        final CountDownLatch calcDone = new CountDownLatch(1);
        Thread ct = new Thread(() -> {
            background.calculate();
            try {
                background.calculate();
            } catch (final Throwable t) {
                calcError.set(t);
            }
            calcDone.countDown();
        });
        ct.start();
        calcDone.await(1, TimeUnit.SECONDS);

        assertNotNull(calcError.get());
        assertEquals("The pipeline is broken", calcError.get().getMessage());
        assertFalse(ct.isAlive(), "Calc thread should not hang");
    }

    @Test
    void mediaPipelineCrash_setsBrokenState() throws Exception {
        setUpMockPipeline(
                reducerSteps(), fakeReducer(), fakePreparer(),
                p -> doThrow(new OutOfMemoryError("Vulkan device lost")).when(p.marker()).mark(any(), any(), anyInt()));

        final var media = underTest.mediaFrame();
        final var background = underTest.background();
        final ByteBuffer frame0 = fakeMediaBytes(LOTS);
        final ByteBuffer frame1 = fakeMediaBytes(LOTS);

        final CountDownLatch calcPhase1 = new CountDownLatch(1);
        final CountDownLatch mediaThrew = new CountDownLatch(1);
        final AtomicReference<Throwable> calcError = new AtomicReference<>();
        final CountDownLatch calcDone = new CountDownLatch(1);

        media.accept(frame0, 0);

        Thread ct = new Thread(() -> {
            background.calculate();
            calcPhase1.countDown();
            try {
                mediaThrew.await(1, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                background.calculate();
            } catch (final Throwable t) {
                calcError.set(t);
            }
            calcDone.countDown();
        });
        ct.start();

        calcPhase1.await(1, TimeUnit.SECONDS);
        media.accept(frame1, 1);
        mediaThrew.countDown();

        calcDone.await(1, TimeUnit.SECONDS);
        assertNotNull(calcError.get());
        assertEquals("The pipeline is broken", calcError.get().getMessage());
        assertFalse(ct.isAlive(), "Calc thread should not hang");
    }

    @Test
    void userInterrupt_notBroken_preservesInterrupt() throws Exception {
        setUpMockPipeline(minimalFakes());

        final var background = underTest.background();

        final AtomicReference<Boolean> wasInterrupted = new AtomicReference<>();
        final CountDownLatch calcDone = new CountDownLatch(1);
        Thread ct = new Thread(() -> {
            background.calculate();
            wasInterrupted.set(Thread.interrupted());
            calcDone.countDown();
        });
        ct.start();

        while (ct.getState() != Thread.State.WAITING && ct.isAlive()) {
            Thread.sleep(5);
        }
        ct.interrupt();

        calcDone.await(1, TimeUnit.SECONDS);
        assertTrue(wasInterrupted.get(), "User interrupt must be preserved");
        assertFalse(ct.isAlive(), "Calc thread should not hang");
    }
}