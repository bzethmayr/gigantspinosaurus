package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_VERSION;

public class SpatialReduction implements ReducesMedia, AutoCloseable {
    private final VideoPipeline pipeline;
    private final ReductionStep[] steps;

    public SpatialReduction(final GpuContext context, final int inWidth, final int inHeight) {
        this.pipeline = new VideoPipeline(context, inWidth, inHeight);
        this.steps = new ReductionStep[]{new ReductionStep(SPATIAL_ID, SPATIAL_VERSION)};
    }

    @Override
    public ReductionStep[] reductions() {
        return steps;
    }

    @Override
    public ByteBuffer apply(final ByteBuffer yBuffer) {
        return pipeline.execute(yBuffer);
    }

    @Override
    public void close() throws Exception {
        pipeline.close();
    }
}
