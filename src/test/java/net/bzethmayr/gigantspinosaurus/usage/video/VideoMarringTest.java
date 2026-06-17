package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.usage.BindsMarkingPipeline;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.bzethmayr.gigantspinosaurus.usage.video.WorkerState.APPLY_MARK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoMarringTest implements TestsModel, TestsWithBytes, TestsWithFakePipelines {
    private static final int CADENCE = 60;
    private static final int EMPTY = 10;

    private VideoMarring underTest;

    @SafeVarargs
    private void setUpMockPipeline(final Consumer<BindsMarkingPipeline>... configs) {
        underTest = mockPipeline(CADENCE, EMPTY, configs).marring();
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
        setUpMockPipeline(reducerSteps(),
                p -> doThrow(deviceLost()).when(p.reducer()).apply(any()),
                fakePreparer());

        final var coordinator = underTest.coordinator();
        final var background = underTest.background();
        final ByteBuffer rawFrame = fakeMediaBytes(LOTS);

        underTest.mediaFrame().accept(rawFrame, 0);

        final Thread ct = new Thread(background::calculate);
        ct.start();
        ct.join(1000);

        assertTrue(coordinator.isBroken(), "Pipeline should be broken after reducer crash");
        assertFalse(ct.isAlive(), "Calc thread should not hang");
    }

    @Test
    void mediaPipelineCrash_setsBrokenState() throws Exception {
        setUpMockPipeline(reducerSteps(), fakeReducer(), fakePreparer(), p ->
                doThrow(deviceLost()).when(p.marker()).mark(any(), any(), anyInt()));

        final var coordinator = underTest.coordinator();
        final var background = underTest.background();
        final ByteBuffer frame0 = fakeMediaBytes(LOTS);
        final ByteBuffer frame1 = fakeMediaBytes(LOTS);

        underTest.mediaFrame().accept(frame0, 0);

        final Thread ct = new Thread(background::calculate);
        ct.start();

        while (coordinator.getState() != APPLY_MARK && ct.isAlive()) {
            Thread.sleep(5);
        }
        underTest.mediaFrame().accept(frame1, 1);
        ct.join(1000);

        assertTrue(coordinator.isBroken(), "Pipeline should be broken after marker crash");
        assertFalse(ct.isAlive(), "Calc thread should not hang");
    }

    @Test
    void userInterrupt_notBroken_preservesInterrupt() throws Exception {
        setUpMockPipeline(minimalFakes());

        final var background = underTest.background();

        final AtomicReference<Boolean> wasInterrupted = new AtomicReference<>();
        final CountDownLatch calcDone = new CountDownLatch(1);
        final Thread ct = new Thread(() -> {
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
