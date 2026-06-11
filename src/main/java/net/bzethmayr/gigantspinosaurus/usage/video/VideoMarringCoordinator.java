package net.bzethmayr.gigantspinosaurus.usage.video;

public interface VideoMarringCoordinator {

    // === Media thread bracket ===
    void mediaEnter();
    void mediaLeave();

    // === Calc thread bracket ===
    void calcEnter();
    void calcLeave();

    // === Under the lock (called between enter/leave) ===
    WorkerState getState();
    void setState(WorkerState newState);
    void unparkCalc();
    void unparkMedia();

    // === Error handling ===
    void pipelineBroken();
    boolean isBroken();
    void reset();

    // === Factory ===
    static VideoMarringCoordinator blockingCoordinator() {
        return new BlockingMarringCoordinator();
    }

    static VideoMarringCoordinator nonBlockingCoordinator() {
        return new NonBlockingMarringCoordinator();
    }
}
