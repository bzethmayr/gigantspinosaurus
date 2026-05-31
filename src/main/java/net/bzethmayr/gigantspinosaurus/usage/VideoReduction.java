package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION;

public class VideoReduction implements ReducesMedia, AutoCloseable {
    private final SpatialReduction spatial;

    public VideoReduction(final SpatialReduction spatial) {
        this.spatial = spatial;
    }

    public VideoReduction(final GpuContext context, final int width, final int height) {
        this(new SpatialReduction(context, width, height,
                new ReductionStep(YCBCR_ID, YCBCR_VERSION)));
    }

    public VideoReduction(final GpuContext context, final int width, final int height,
                          final ReductionStep... optionalSteps) {
        this(new SpatialReduction(context, width, height, optionalSteps));
    }

    @Override
    public ReductionStep[] reductions() {
        return spatial.reductions();
    }

    @Override
    public ByteBuffer apply(final ByteBuffer rgbBuffer) {
        return spatial.apply(rgbBuffer);
    }

    @Override
    public void close() throws Exception {
        spatial.close();
    }
}
