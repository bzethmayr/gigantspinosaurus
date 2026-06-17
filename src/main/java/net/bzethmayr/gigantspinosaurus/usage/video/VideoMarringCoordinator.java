package net.bzethmayr.gigantspinosaurus.usage.video;

/**
 * External coordinator API.
 */
public sealed interface VideoMarringCoordinator permits BlockingMarringCoordinator, MarringCoordinatorAccess, NonBlockingMarringCoordinator {

    /**
     * Returns the current (volatile) state at the time of call.
     * @return the current state.
     */
    WorkerState getState();

    /**
     * Whether either thread broke.
     * @return true if a thread broke, otherwise false.
     */
    boolean isBroken();

    /**
     * Returns the exception, if any, that broke a thread.
     * @return an exception.
     */
    Throwable brokeWith();

    /**
     * Resets the process.
     */
    void reset();

    /**
     * Returns a blocking coordinator that waits until states are reached.
     * @return a blocking coordinator.
     */
    static VideoMarringCoordinator blockingCoordinator() {
        return new BlockingMarringCoordinator();
    }

    /**
     * Returns a nonblocking coordinator that no-ops on unmatched states.
     * @return a nonblocking coordinator.
     */
    static VideoMarringCoordinator nonBlockingCoordinator() {
        return new NonBlockingMarringCoordinator();
    }
}
