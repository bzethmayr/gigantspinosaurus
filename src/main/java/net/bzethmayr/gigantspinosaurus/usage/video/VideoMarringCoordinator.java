package net.bzethmayr.gigantspinosaurus.usage.video;

public sealed interface VideoMarringCoordinator permits BlockingMarringCoordinator, MarringCoordinatorAccess, NonBlockingMarringCoordinator {

    WorkerState getState();
    boolean isBroken();
    Throwable brokeWith();
    void reset();

    static VideoMarringCoordinator blockingCoordinator() {
        return new BlockingMarringCoordinator();
    }

    static VideoMarringCoordinator nonBlockingCoordinator() {
        return new NonBlockingMarringCoordinator();
    }
}
