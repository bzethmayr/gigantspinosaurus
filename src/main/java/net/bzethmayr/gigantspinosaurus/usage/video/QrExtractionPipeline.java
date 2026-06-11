package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.usage.BindsExtractionPipeline;
import net.bzethmayr.gigantspinosaurus.usage.qr.ZxingDecodesMar;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;

public final class QrExtractionPipeline {
    private QrExtractionPipeline() {
        throw becauseStaticsOnly();
    }

    public static BindsExtractionPipeline videoTimExtraction(final int width, final int height) {
        return new BindsExtractionPipeline(
                () -> new RollingBufferExtractsMarks(5, 1, width, height),
                new ZxingDecodesMar(width, height)
        );
    }
}
