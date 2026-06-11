package net.bzethmayr.gigantspinosaurus.usage.video;

import java.util.concurrent.locks.ReentrantLock;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

final class NonBlockingMarringCoordinator implements VideoMarringCoordinator {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile WorkerState state = WorkerState.GRAB_FRAME;
    private volatile boolean broken;

    private Thread mediaThread;
    private Thread calcThread;

    @Override
    public void mediaEnter() {
        registerMedia(Thread.currentThread());
        lock.lock();
    }

    @Override
    public void mediaLeave() {
        lock.unlock();
    }

    @Override
    public void calcEnter() {
        registerCalc(Thread.currentThread());
        lock.lock();
    }

    @Override
    public void calcLeave() {
        lock.unlock();
    }

    @Override
    public WorkerState getState() {
        return state;
    }

    @Override
    public void setState(final WorkerState newState) {
        state = newState;
    }

    @Override
    public void unparkCalc() {
    }

    @Override
    public void unparkMedia() {
    }

    @Override
    public void pipelineBroken() {
        broken = true;
        state = WorkerState.BROKEN;
        if (mediaThread != null) {
            mediaThread.interrupt();
        }
        if (calcThread != null) {
            calcThread.interrupt();
        }
    }

    @Override
    public boolean isBroken() {
        return broken;
    }

    @Override
    public void reset() {
        lock.lock();
        try {
            state = WorkerState.GRAB_FRAME;
            broken = false;
        } finally {
            lock.unlock();
        }
    }

    private void registerMedia(final Thread current) {
        if (mediaThread == null) {
            mediaThread = current;
        } else if (mediaThread != current) {
            throw becauseIllegal("Non-media thread %s acting as media thread", current);
        }
        if (mediaThread == calcThread) {
            throw becauseIllegal("The media and calculation threads must be distinct");
        }
    }

    private void registerCalc(final Thread current) {
        if (calcThread == null) {
            calcThread = current;
        } else if (calcThread != current) {
            throw becauseIllegal("Non-calculation thread %s acting as calc thread", current);
        }
        if (mediaThread == calcThread) {
            throw becauseIllegal("The media and calculation threads must be distinct");
        }
    }
}
