package net.bzethmayr.gigantspinosaurus.usage.video;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.bzethmayr.gigantspinosaurus.usage.video.WorkerState.*;
import static org.junit.jupiter.api.Assertions.*;

class BlockingMarringCoordinatorTest {

    @Test
    void initialState_isGrabbingAndNotBroken() {
        final var underTest = new BlockingMarringCoordinator();
        assertEquals(GRAB_FRAME, underTest.getState());
        assertFalse(underTest.isBroken());
    }

    @Test
    void setStateAndGetState_roundTrip() {
        final var underTest = new BlockingMarringCoordinator();
        underTest.setState(CALCULATE_MARK);
        assertEquals(CALCULATE_MARK, underTest.getState());
    }

    @Test
    void pipelineBroken_setsBrokenState() {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        final var t = new RuntimeException("gpu crash");
        underTest.pipelineBroken(t);
        underTest.mediaLeave();
        assertTrue(underTest.isBroken());
        assertEquals(BROKEN, underTest.getState());
        assertSame(t, underTest.brokeWith());
    }

    @Test
    void reset_clearsBrokenState() {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        underTest.pipelineBroken(new RuntimeException());
        underTest.reset();
        underTest.mediaLeave();
        assertFalse(underTest.isBroken());
        assertEquals(GRAB_FRAME, underTest.getState());
        assertNull(underTest.brokeWith());
    }

    @Test
    void mediaEnter_thenLeave() {
        final var underTest = new BlockingMarringCoordinator();
        assertDoesNotThrow(() -> {
            underTest.mediaEnter();
            underTest.mediaLeave();
        });
    }

    @Test
    void calcEnter_whenStateIsCalculateMark_completesOnSeparateThread() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
        final var done = new CountDownLatch(1);

        underTest.mediaEnter();
        underTest.setState(CALCULATE_MARK);
        underTest.mediaLeave();

        final var ct = new Thread(() -> {
            underTest.calcEnter();
            underTest.calcLeave();
            done.countDown();
        });
        ct.start();

        assertTrue(done.await(2000, TimeUnit.MILLISECONDS));
        ct.join(1000);
    }

    @Test
    void calcEnter_blocksUntilUnparked() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
        final var calcDone = new AtomicBoolean(false);

        final var ct = new Thread(() -> {
            underTest.calcEnter();
            calcDone.set(true);
            underTest.calcLeave();
        });

        underTest.mediaEnter();
        ct.start();

        Thread.sleep(100);
        assertFalse(calcDone.get());

        underTest.setState(CALCULATE_MARK);
        underTest.unparkCalc();
        underTest.mediaLeave();

        ct.join(2000);
        assertTrue(calcDone.get());
    }

    @Test
    void calcEnter_whenBroken_doesNotBlock() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        underTest.pipelineBroken(new RuntimeException());
        underTest.mediaLeave();

        final var ct = new Thread(() -> {
            underTest.calcEnter();
            underTest.calcLeave();
        });
        ct.start();
        ct.join(1000);
        assertFalse(ct.isAlive());
    }

    @Test
    void mediaEnter_secondDifferentThread_throws() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
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
    void calcEnter_sameThreadAsMedia_throws() {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        assertThrows(IllegalArgumentException.class, underTest::calcEnter);
        underTest.mediaLeave();
    }

    @Test
    void calcEnter_secondDifferentThread_throws() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
        final var calcDone = new CountDownLatch(1);

        final var ct = new Thread(() -> {
            underTest.calcEnter();
            underTest.calcLeave();
            calcDone.countDown();
        });
        ct.start();
        Thread.sleep(200);

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.calcEnter();
            underTest.calcLeave();
        });

        underTest.mediaEnter();
        underTest.setState(CALCULATE_MARK);
        underTest.unparkCalc();
        underTest.mediaLeave();

        assertTrue(calcDone.await(2000, TimeUnit.MILLISECONDS));
        ct.join(1000);
    }

    @Test
    void unparkCalc_requiresHeldLock_doesNotThrow() {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        assertDoesNotThrow(underTest::unparkCalc);
        underTest.mediaLeave();
    }

    @Test
    void unparkCalc_withoutLock_doesNotThrow() {
        final var underTest = new BlockingMarringCoordinator();
        assertDoesNotThrow(underTest::unparkCalc);
    }

    @Test
    void pipelineBroken_withoutLock_setsBrokenState() {
        final var underTest = new BlockingMarringCoordinator();
        final var t = new RuntimeException("crash");
        assertDoesNotThrow(() -> underTest.pipelineBroken(t));
        assertTrue(underTest.isBroken());
        assertEquals(BROKEN, underTest.getState());
        assertSame(t, underTest.brokeWith());
    }

    @Test
    void reset_underLock_doesNotDeadlock() throws Exception {
        final var underTest = new BlockingMarringCoordinator();
        underTest.mediaEnter();
        final var resetDone = new AtomicBoolean(false);

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
}
