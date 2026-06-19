package net.bzethmayr.gigantspinosaurus.usage.video;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.bzethmayr.gigantspinosaurus.usage.video.WorkerState.*;

public class VideoMarringTestCoordination implements AutoCloseable {
    private final VideoMarring marring;
    private final Semaphore calcDone = new Semaphore(0);
    private final AtomicBoolean done = new AtomicBoolean();
    private final Thread background;

    public VideoMarringTestCoordination(final VideoMarring marring) {
        this.marring = marring;
        marring.coordinator().afterCalc(APPLY_MARK, calcDone::release);
        marring.coordinator().afterMedia(CALCULATE_MARK, () -> {
            try {
                calcDone.tryAcquire(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        background = new Thread(() -> {
            while (!done.get()) {
                if (marring.coordinator().getState() == CALCULATE_MARK) {
                    marring.background().calculate();
                }
            }
        });
        background.start();
    }

    public void sendFrame(final ByteBuffer frame, final int index) {
        marring.mediaFrame().accept(frame, index);
    }

    @Override
    public void close() {
        done.set(true);
        try {
            background.join(2000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
