package net.bzethmayr.gigantspinosaurus.usage.video;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.bzethmayr.gigantspinosaurus.usage.video.VideoMarringCoordinator.nonBlockingCoordinator;
import static net.bzethmayr.gigantspinosaurus.usage.video.WorkerState.*;
import static org.junit.jupiter.api.Assertions.*;

class NonBlockingMarringCoordinatorTest implements  TestsWithFakePipelines {

    private MarringCoordinatorAccess underTest;

    @BeforeEach
    void setUpUnderTest() {
        underTest = (MarringCoordinatorAccess) nonBlockingCoordinator();
    }

    @Test
    void initialState_isGrabbingAndNotBroken() {
        assertEquals(GRAB_FRAME, underTest.getState());
        assertFalse(underTest.isBroken());
    }

    @Test
    void setStateAndGetState_roundTrip() {
        underTest.setState(CALCULATE_MARK);
        assertEquals(CALCULATE_MARK, underTest.getState());
        underTest.setState(APPLY_MARK);
        assertEquals(APPLY_MARK, underTest.getState());
    }

    @Test
    void pipelineBroken_setsBrokenState() {
        final var t = gpuCrash();

        underTest.pipelineBroken(t);

        assertTrue(underTest.isBroken());
        assertEquals(BROKEN, underTest.getState());
        assertSame(t, underTest.brokeWith());
    }

    @Test
    void pipelineBroken_withNoRegisteredThreads_doesNotThrow() {
        assertDoesNotThrow(() -> underTest.pipelineBroken(gpuCrash()));
        assertTrue(underTest.isBroken());
    }

    @Test
    void reset_clearsBrokenStateAndResetsToGrabbing() {
        underTest.pipelineBroken(gpuCrash());

        underTest.reset();

        assertEquals(GRAB_FRAME, underTest.getState());
        assertFalse(underTest.isBroken());
        assertNull(underTest.brokeWith());
    }

    @Test
    void mediaEnter_thenLeave() {
        assertDoesNotThrow(() -> {
            underTest.mediaEnter();
            underTest.mediaLeave();
        });
    }

    @Test
    void calcEnter_thenLeave() {
        assertDoesNotThrow(() -> {
            underTest.calcEnter();
            underTest.calcLeave();
        });
    }

    @Test
    void mediaEnter_secondDifferentThread_throws() throws Exception {
        underTest.mediaEnter();
        underTest.mediaLeave();

        final var ct = new Thread(() ->
                assertThrows(IllegalArgumentException.class, () -> {
                    underTest.mediaEnter();
                    underTest.mediaLeave();
                }));
        ct.start();
        ct.join(1000);
    }

    @Test
    void calcEnter_secondDifferentThread_throws() throws Exception {
        underTest.calcEnter();
        underTest.calcLeave();

        final var ct = new Thread(() ->
                assertThrows(IllegalArgumentException.class, () -> {
                    underTest.calcEnter();
                    underTest.calcLeave();
                }));
        ct.start();
        ct.join(1000);
    }

    @Test
    void mediaAndCalcMustBeDistinctThreads() throws Exception {
        final var ct = new Thread(() -> {
            underTest.calcEnter();
            assertThrows(IllegalArgumentException.class, underTest::mediaEnter);
            underTest.calcLeave();
        });
        ct.start();
        ct.join(1000);
    }

    @Test
    void reset_underLock_doesNotDeadlock() throws Exception {
        underTest.mediaEnter();
        final AtomicBoolean resetDone = new AtomicBoolean(false);

        final var t = new Thread(() -> {
            underTest.reset();
            resetDone.set(true);
        });
        t.start();

        assertFalse(resetDone.get());
        underTest.mediaLeave();
        t.join(1000);
        assertTrue(resetDone.get());
    }

    @Test
    void unparkMethods_areNoOps() {
        assertDoesNotThrow(underTest::unparkCalc);
        assertDoesNotThrow(underTest::unparkMedia);
    }
}
