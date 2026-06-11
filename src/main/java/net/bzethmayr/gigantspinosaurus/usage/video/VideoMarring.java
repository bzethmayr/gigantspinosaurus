package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.usage.BindsConstructors;
import net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment;
import net.bzethmayr.gigantspinosaurus.usage.BindsMarkingPipeline;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

/**
 * Marring video in near-real time requires
 * offloading as much work as possible away from the media thread.
 */
public class VideoMarring {
    private final VideoMarringCoordinator coordinator;
    private final BindsMarkingPipeline pipeline;
    private final MarCreation.ReducedFrameReceiver marsReduced;
    private ByteBuffer copyBuffer;
    private final ByteBuffer mark;
    private int mediaIndex;
    private int copiedIndex;
    private final int cadenceFrames;
    private final int emptyFrames;
    private final MediaFrameAcceptor mediaFrames;
    private final CalculationThreadWorker background;

    public VideoMarring(
            final BindsConstructors ctors,
            final BindsEnvironment env,
            final BindsMarkingPipeline pipeline,
            final VideoMarringCoordinator coordinator,
            final int cadenceFrames,
            final int emptyFrames
    ) {
        this.pipeline = pipeline;
        this.coordinator = coordinator;
        this.cadenceFrames = cadenceFrames;
        this.emptyFrames = emptyFrames;
        final MarCreation marCreator = new MarCreation(ctors, env);
        final ExposesMar intent = marCreator.intentFrame(pipeline.reducer().reductions());
        mark = pipeline.encoder().emptyMark(intent.canonicalBytes().length);
        this.marsReduced = marCreator.intentToRecord(intent);
        mediaFrames = new FrameThreadWorker();
        background = new CalculationThreadWorker();
    }

    public VideoMarringCoordinator coordinator() {
        return coordinator;
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

    private void itsBroken() {
        Thread.interrupted();
        throw becauseImpossible("The pipeline is broken");
    }

    private class FrameThreadWorker implements MediaFrameAcceptor {
        private int displayUntil;
        private int firstDisplay;
        private int firstEmpty;

        @Override
        public void accept(final ByteBuffer rawBuffer, final Integer index) {
            mediaIndex = index;
            coordinator.mediaEnter();
            try {
                if (coordinator.isBroken()) itsBroken();
                switch (coordinator.getState()) {
                    case GRAB_FRAME -> {
                        if (copyBuffer == null || copyBuffer.capacity() != rawBuffer.limit()) {
                            copyBuffer = ByteBuffer.allocate(rawBuffer.limit());
                        }
                        copiedIndex = mediaIndex;
                        displayUntil = copiedIndex + cadenceFrames;
                        firstDisplay = Integer.MIN_VALUE;
                        firstEmpty = Integer.MIN_VALUE;
                        copyBuffer.clear();
                        copyBuffer.put(rawBuffer);
                        copyBuffer.flip();
                        coordinator.setState(WorkerState.CALCULATE_MARK);
                        coordinator.unparkCalc();
                    }
                    case CALCULATE_MARK -> {
                        coordinator.unparkCalc();
                    }
                    case APPLY_MARK -> {
                        pipeline.marker().mark(mark, rawBuffer, mediaIndex);
                        mark.rewind();
                        if (firstDisplay < 0) {
                            firstDisplay = mediaIndex;
                        }
                        if (mediaIndex > displayUntil && mediaIndex > firstDisplay + emptyFrames) {
                            displayUntil = mediaIndex + emptyFrames;
                            coordinator.setState(WorkerState.WAIT_EMPTY);
                        }
                    }
                    case WAIT_EMPTY -> {
                        if (firstEmpty < 0) {
                            firstEmpty = mediaIndex;
                        }
                        if (mediaIndex > displayUntil && mediaIndex > firstEmpty + emptyFrames) {
                            coordinator.setState(WorkerState.GRAB_FRAME);
                        }
                    }
                    case BROKEN -> itsBroken();
                }
            } catch (final Throwable t) {
                coordinator.pipelineBroken();
            } finally {
                coordinator.mediaLeave();
            }
        }
    }

    private class CalculationThreadWorker implements BackgroundCalculator {
        @Override
        public void calculate() {
            coordinator.calcEnter();
            try {
                if (coordinator.isBroken()) itsBroken();
                if (coordinator.getState() == WorkerState.CALCULATE_MARK) {
                    final ByteBuffer mediaFrame = copyBuffer;
                    final ByteBuffer reduced = pipeline.reducer().apply(mediaFrame);
                    final ExposesMar mar = marsReduced.reducedFrame(reduced, copiedIndex);
                    mark.clear();
                    pipeline.encoder().accept(mar.canonicalBytes(), mark);
                    coordinator.setState(WorkerState.APPLY_MARK);
                    coordinator.unparkMedia();
                }
            } catch (final Throwable t) {
                coordinator.pipelineBroken();
            } finally {
                coordinator.calcLeave();
            }
        }
    }
}
