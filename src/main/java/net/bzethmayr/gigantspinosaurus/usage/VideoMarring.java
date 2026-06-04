package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import static java.lang.Thread.currentThread;
import static net.bzethmayr.gigantspinosaurus.usage.VideoMarring.WorkerState.*;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

/**
 * Marring video in near-real time requires
 * offloading as much work as possible away from the media thread.
 */
public class VideoMarring {
    private final Locking locking = new Locking();
    private final BindsMediaPipeline pipeline;
    private final MarCreation.ReducedFrameReceiver marsReduced;
    private volatile WorkerState workerState = GRAB_FRAME;
    private final AtomicReference<ByteBuffer> mediaRef = new AtomicReference<>();
    private final ByteBuffer mark;
    private volatile int mediaIndex;
    private volatile int copiedIndex;
    private final int cadenceFrames;
    private final int emptyFrames;
    private final MediaFrameAcceptor mediaFrames;
    private final CalculationThreadWorker background;
    protected enum WorkerState {
        GRAB_FRAME,
        CALCULATE_MARK,
        APPLY_MARK,
        WAIT_EMPTY,
        BROKEN
    }

    private static final class Locking {
        private final Object copyLock = new Object();
        private volatile Thread mediaThread;
        private volatile Thread calcThread;
    }

    public VideoMarring(
            final BindsConstructors ctors,
            final BindsEnvironment env,
            final BindsMediaPipeline pipeline,
            final int cadenceFrames,
            final int emptyFrames
    ) {
        this.pipeline = pipeline;
        this.cadenceFrames = cadenceFrames;
        this.emptyFrames = emptyFrames;
        final MarCreation marCreator = new MarCreation(ctors, env);
        final ExposesMar intent = marCreator.intentFrame(pipeline.reducer().reductions());
        mark = pipeline.combiner().emptyMark(intent.canonicalBytes().length);
        this.marsReduced = marCreator.intentToRecord(intent);
        mediaFrames = new FrameThreadWorker();
        background = new CalculationThreadWorker();
    }

    @FunctionalInterface
    public interface MediaFrameAcceptor extends BiConsumer<ByteBuffer, Integer> {
    }

    @FunctionalInterface
    public interface BackgroundCalculator {
        /**
         * Parks the thread and waits for media frames - when a frame is available,
         * calculates the mark that verifies that frame and goes back to waiting for the next frame.
         */
        void calculate();
    }

    public MediaFrameAcceptor mediaFrame() {
        return mediaFrames;
    }

    public BackgroundCalculator background() {
        return background;
    }

    private ByteBuffer initCopyBuffer(final ByteBuffer sourceBuffer, final AtomicReference<ByteBuffer> copyRef) {
        if (copyRef.get() == null)
            synchronized (locking.copyLock) {
                if (copyRef.get() == null) {
                    final ByteBuffer copyBuffer = ByteBuffer.allocate(sourceBuffer.limit());
                    copyRef.compareAndSet(null, copyBuffer);
                }
            }
        return copyRef.get();
    }

    private IllegalArgumentException becauseSameThreads() {
        return becauseIllegal("The media and calculation threads must be distinct");
    }

    private void snaffleMediaThread() {
        final Thread current = currentThread();
        if (locking.mediaThread == null) {
            locking.mediaThread = current;
        } else if (current != locking.mediaThread) {
            throw becauseIllegal("Non-media thread %s acting as media thread", current);
        }
        if (locking.mediaThread == locking.calcThread) {
            throw becauseSameThreads();
        }
    }

    private void snaffleCalcThread() {
        final Thread current = currentThread();
        if (locking.calcThread == null) {
            locking.calcThread = current;
        } else if (current != locking.calcThread) {
            throw becauseIllegal("Non-calculation thread %s acting as calc thread", current);
        }
        if (locking.calcThread == locking.mediaThread) {
            throw becauseSameThreads();
        }
    }

    private class FrameThreadWorker implements MediaFrameAcceptor {
        private volatile int displayUntil;
        private volatile int firstDisplay;
        private volatile int firstEmpty;

        @Override
        public void accept(ByteBuffer rawBuffer, Integer index) {
            snaffleMediaThread();
            mediaIndex = index;
            final WorkerState current = workerState;
            try {
                switch (current) {
                    case GRAB_FRAME -> {
                        final ByteBuffer in = initCopyBuffer(rawBuffer, mediaRef);
                        copiedIndex = mediaIndex;
                        displayUntil = copiedIndex + cadenceFrames;
                        firstDisplay = Integer.MIN_VALUE;
                        firstEmpty = Integer.MIN_VALUE;
                        in.clear();
                        in.put(rawBuffer);
                        in.flip();
                        workerState = WorkerState.CALCULATE_MARK;
                        LockSupport.unpark(locking.calcThread);
                    }
                    case CALCULATE_MARK -> LockSupport.unpark(locking.calcThread); // buffers belong to calculation
                    case APPLY_MARK -> {
                        pipeline.marker().mark(mark, rawBuffer);
                        mark.rewind();
                        if (firstDisplay < 0) {
                            firstDisplay = mediaIndex;
                        }
                        if (mediaIndex > displayUntil && mediaIndex > firstDisplay + emptyFrames) {
                            displayUntil = mediaIndex + emptyFrames;
                            workerState = WAIT_EMPTY;
                        }
                    }
                    case WAIT_EMPTY -> {
                        if (firstEmpty < 0) {
                            firstEmpty = mediaIndex;
                        }
                        if (mediaIndex > displayUntil && mediaIndex > firstEmpty + emptyFrames) {
                            workerState = GRAB_FRAME;
                        }
                    }
                    case BROKEN -> itsBroken();
                }
            } catch (final Throwable t) {
                weBrokeThis(locking.calcThread);
            }
        }
    }

    private void weBrokeThis(final Thread partner) {
        workerState = BROKEN;
        partner.interrupt();
    }

    private void waitToProceed() {
        LockSupport.park(); // buffers belong to the media thread
    }

    private void itsBroken() {
        Thread.interrupted();
        throw becauseImpossible("The pipeline is broken");
    }

    private class CalculationThreadWorker implements BackgroundCalculator {
        @Override
        public void calculate() {
            snaffleCalcThread();
            final WorkerState current = workerState;
            switch (current) {
                case GRAB_FRAME, APPLY_MARK, WAIT_EMPTY -> waitToProceed(); // buffers belong to the media thread
                case BROKEN -> itsBroken();
                case CALCULATE_MARK -> {
                    try {
                        final ByteBuffer mediaFrame = mediaRef.get();
                        final ByteBuffer reduced = pipeline.reducer().apply(mediaFrame);
                        final ExposesMar mar = marsReduced.reducedFrame(reduced, copiedIndex);
                        mark.clear();
                        pipeline.combiner().accept(mar.canonicalBytes(), mark);
                        mark.flip();
                        workerState = APPLY_MARK;
                    } catch (final Throwable t) {
                        weBrokeThis(locking.mediaThread);
                    } finally {
                        LockSupport.unpark(locking.mediaThread);
                    }
                }
            }
        }
    }
}
