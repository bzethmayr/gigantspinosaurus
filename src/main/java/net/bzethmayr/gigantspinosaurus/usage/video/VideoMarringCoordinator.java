package net.bzethmayr.gigantspinosaurus.usage.video;

import com.sun.jna.platform.win32.COM.IRunningObjectTable;

import java.util.EnumMap;
import java.util.Map;

/**
 * External coordinator API.
 */
public sealed interface VideoMarringCoordinator
        permits MarringCoordinatorAccess, VideoMarringCoordinator.ObservableMarringCoordinator, BlockingMarringCoordinator, NonBlockingMarringCoordinator {

    abstract non-sealed class ObservableMarringCoordinator implements VideoMarringCoordinator, MarringCoordinatorAccess {
        protected final Map<WorkerState, Runnable> afterMedia = new EnumMap<>(WorkerState.class);
        protected final Map<WorkerState, Runnable> afterCalc = new EnumMap<>(WorkerState.class);

        @Override
        public void afterMedia(final WorkerState state, final Runnable action) {
            afterMedia.put(state, action);
        }

        @Override
        public void afterCalc(final WorkerState state, final Runnable action) {
            afterCalc.put(state, action);
        }

        protected void afterMedia(final WorkerState lockedState) {
            final Runnable action = afterMedia.get(lockedState);
            if (action != null) {
                action.run();
            }
        }

        protected void afterCalc(final WorkerState lockedState) {
            final Runnable action = afterCalc.get(lockedState);
            if (action != null) {
                action.run();
            }
        }
    }

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

    void afterCalc(WorkerState state, Runnable action);
    void afterMedia(WorkerState state, Runnable action);

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
