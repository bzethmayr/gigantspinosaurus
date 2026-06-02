package net.bzethmayr.gigantspinosaurus.usage.pipelines;

import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;

import java.nio.ByteBuffer;

public class SpatialReduction implements ReducesMedia, AutoCloseable {
    private final VideoPipeline pipeline;

    public SpatialReduction(final GpuContext context, final int inWidth, final int inHeight,
                            final ReductionStep... optionalSteps) {
        this.pipeline = new VideoPipeline(context, inWidth, inHeight, optionalSteps);
    }

    @Override
    public ReductionStep[] reductions() {
        return pipeline.reductionSteps();
    }

    @Override
    public ByteBuffer apply(final ByteBuffer rgbBuffer) {
        return pipeline.execute(rgbBuffer);
    }

    @Override
    public void close() throws Exception {
        pipeline.close();
    }
}
