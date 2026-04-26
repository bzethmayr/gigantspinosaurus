package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import static java.lang.Thread.currentThread;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public class MediaMarring {
    private final Object copyLock = new Object();
    private final BindsMediaPipeline pipeline;
    private final MarCreation.ReducedFrameReceiver marsReduced;
    private volatile int workerState;
    private volatile Thread mediaThread;
    private final AtomicReference<ByteBuffer> mediaRef = new AtomicReference<>();
    private volatile Thread calcThread;
    private final ByteBuffer mark;
    private volatile int mediaIndex;
    private volatile int copiedIndex;
    private final int cadenceFrames;
    private final int emptyFrames;
    private final MediaFrameAcceptor mediaFrames;
    private final CalculationThreadWorker background;

    public MediaMarring(
            final BindsConstructors ctors,
            final BindsEnvironment env,
            final BindsMediaPipeline pipeline,
            final int cadenceFrames,
            final int emptyFrames
    ) {
        this.pipeline = pipeline;
        this.cadenceFrames = cadenceFrames;
        this.emptyFrames = emptyFrames;
        this.marsReduced = new MarCreation(ctors, env).intentToRecord(pipeline.reducer().reductions());
        mark = pipeline.combiner().emptyMark();
        mediaFrames = new FrameThreadWorker();
        background = new CalculationThreadWorker();
    }

    @FunctionalInterface
    public interface MediaFrameAcceptor extends BiConsumer<ByteBuffer, Integer> {
    }

    @FunctionalInterface
    public interface BackgroundCalculator {
        void calculate();
    }

    public MediaFrameAcceptor mediaFrame() {
        return mediaFrames;
    }

    public BackgroundCalculator background() {
        return background;
    }

    protected static IllegalStateException becauseBadWorkerState(final int state) {
        return becauseImpossible("Unknown worker state %s", state);
    }

    private ByteBuffer initCopyBuffer(final ByteBuffer sourceBuffer, final AtomicReference<ByteBuffer> copyRef) {
        if (copyRef.get() == null)
            synchronized (copyLock) {
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
        if (mediaThread == null) {
            mediaThread = current;
        } else if (current != mediaThread) {
            throw becauseIllegal("Non-media thread %s acting as media thread", current);
        } else if (mediaThread == calcThread) {
            throw becauseSameThreads();
        }
    }

    private void snaffleCalcThread() {
        final Thread current = currentThread();
        if (calcThread == null) {
            calcThread = current;
        } else if (current != calcThread) {
            throw becauseIllegal("Non-calculation thread %s acting as calc thread", current);
        } else if (calcThread == mediaThread) {
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
            final int current = workerState;
            switch (current) {
                case 0 -> {
                    final ByteBuffer in = initCopyBuffer(rawBuffer, mediaRef);
                    copiedIndex = mediaIndex;
                    displayUntil = copiedIndex + cadenceFrames;
                    firstDisplay = Integer.MIN_VALUE;
                    firstEmpty = Integer.MIN_VALUE;
                    in.clear();
                    in.put(rawBuffer);
                    in.flip();
                    workerState = 1;
                    LockSupport.unpark(calcThread);
                }
                case 1 -> LockSupport.unpark(calcThread); // buffers belong to calculation
                case 2 -> {
                    pipeline.marker().mark(mark, rawBuffer);
                    mark.rewind();
                    if (firstDisplay < 0) {
                        firstDisplay = mediaIndex;
                    }
                    if (mediaIndex > displayUntil && mediaIndex > firstDisplay + emptyFrames) {
                        displayUntil = mediaIndex + emptyFrames;
                        workerState = 3;
                    }
                }
                case 3 -> {
                    if (firstEmpty < 0) {
                        firstEmpty = mediaIndex;
                    }
                    if (mediaIndex > displayUntil && mediaIndex > firstEmpty + emptyFrames) {
                        workerState = 0;
                    }
                }
                default -> throw becauseBadWorkerState(current);
            }
        }
    }

    private class CalculationThreadWorker implements BackgroundCalculator {
        @Override
        public void calculate() {
            snaffleCalcThread();
            final int current = workerState;
            switch (current) {
                case 0, 2, 3 -> LockSupport.park(); // buffers belong to the media thread
                case 1 -> {
                    final ByteBuffer mediaFrame = mediaRef.get();
                    final ByteBuffer reduced = pipeline.reducer().apply(mediaFrame);
                    final ExposesMar mar = marsReduced.reducedFrame(reduced, copiedIndex);
                    mark.clear();
                    pipeline.combiner().accept(mar.canonicalBytes(), mark);
                    mark.flip();
                    workerState = 2;
                    LockSupport.unpark(mediaThread);
                }
                default -> throw becauseBadWorkerState(current);
            }
        }
    }
}
