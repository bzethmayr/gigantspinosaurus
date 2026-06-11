package net.bzethmayr.gigantspinosaurus.usage.video;

sealed interface MarringCoordinatorAccess extends VideoMarringCoordinator permits BlockingMarringCoordinator, NonBlockingMarringCoordinator {

    void mediaEnter();
    void mediaLeave();

    void calcEnter();
    void calcLeave();

    void setState(WorkerState newState);
    void unparkCalc();
    void unparkMedia();

    void pipelineBroken(Throwable t);
}
