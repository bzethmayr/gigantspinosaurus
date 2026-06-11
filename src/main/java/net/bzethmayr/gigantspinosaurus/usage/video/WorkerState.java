package net.bzethmayr.gigantspinosaurus.usage.video;

public enum WorkerState {
    GRAB_FRAME,
    CALCULATE_MARK,
    APPLY_MARK,
    WAIT_EMPTY,
    BROKEN
}
