package net.bzethmayr.gigantspinosaurus.usage.video;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.bzethmayr.gigantspinosaurus.usage.video.WorkerState.*;
import static org.junit.jupiter.api.Assertions.*;

class NonBlockingMarringCoordinatorTest {

    @Test
    void initialState_isGrabbingAndNotBroken() {
        final var underTest = new NonBlockingMarringCoordinator();
        assertEquals(GRAB_FRAME, underTest.getState());
        assertFalse(underTest.isBroken());
    }

    @Test
    void setStateAndGetState_roundTrip() {
        final var underTest = new NonBlockingMarringCoordinator();
        underTest.setState(CALCULATE_MARK);
        assertEquals(CALCULATE_MARK, underTest.getState());
        underTest.setState(APPLY_MARK);
        assertEquals(APPLY_MARK, underTest.getState());
    }

    @Test
    void pipelineBroken_setsBrokenState() {
        final var underTest = new NonBlockingMarringCoordinator();
        final var t = new RuntimeException("crash");

        underTest.pipelineBroken(t);

        assertTrue(underTest.isBroken());
        assertEquals(BROKEN, underTest.getState());
        assertSame(t, underTest.brokeWith());
    }

    @Test
    void pipelineBroken_withNoRegisteredThreads_doesNotThrow() {
        final var underTest = new NonBlockingMarringCoordinator();
        assertDoesNotThrow(() -> underTest.pipelineBroken(new RuntimeException()));
        assertTrue(underTest.isBroken());
    }

    @Test
    void reset_clearsBrokenStateAndResetsToGrabbing() {
        final var underTest = new NonBlockingMarringCoordinator();
        underTest.pipelineBroken(new RuntimeException("boom"));

        underTest.reset();

        assertEquals(GRAB_FRAME, underTest.getState());
        assertFalse(underTest.isBroken());
        assertNull(underTest.brokeWith());
    }

    @Test
    void mediaEnter_thenLeave() {
        final var underTest = new NonBlockingMarringCoordinator();
        assertDoesNotThrow(() -> {
            underTest.mediaEnter();
            underTest.mediaLeave();
        });
    }

    @Test
    void calcEnter_thenLeave() {
        final var underTest = new NonBlockingMarringCoordinator();
        assertDoesNotThrow(() -> {
            underTest.calcEnter();
            underTest.calcLeave();
        });
    }

    @Test
    void mediaEnter_secondDifferentThread_throws() throws Exception {
        final var underTest = new NonBlockingMarringCoordinator();
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
        final var underTest = new NonBlockingMarringCoordinator();
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
        final var underTest = new NonBlockingMarringCoordinator();
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
        final var underTest = new NonBlockingMarringCoordinator();
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
        final var underTest = new NonBlockingMarringCoordinator();
        assertDoesNotThrow(underTest::unparkCalc);
        assertDoesNotThrow(underTest::unparkMedia);
    }
}
