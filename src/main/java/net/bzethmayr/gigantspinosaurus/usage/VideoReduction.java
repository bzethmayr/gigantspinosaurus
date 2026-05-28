package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.model.media.ColorSpaceReduction;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class VideoReduction implements ReducesMedia, AutoCloseable {
    private final ColorSpaceReduction ycbcr;
    private final SpatialReduction spatial;

    public VideoReduction(final ColorSpaceReduction ycbcr, final SpatialReduction spatial) {
        this.ycbcr = ycbcr;
        this.spatial = spatial;
    }

    public VideoReduction(final GpuContext context, final int width, final int height) {
        this(new ColorSpaceReduction(width, height), new SpatialReduction(context, width, height));
    }

    @Override
    public ReductionStep[] reductions() {
        final var ycbcrSteps = ycbcr.reductions();
        final var spatialSteps = spatial.reductions();
        final var combined = Arrays.copyOf(ycbcrSteps, ycbcrSteps.length + spatialSteps.length);
        System.arraycopy(spatialSteps, 0, combined, ycbcrSteps.length, spatialSteps.length);
        return combined;
    }

    @Override
    public ByteBuffer apply(final ByteBuffer rgbBuffer) {
        final ByteBuffer yBuffer = ycbcr.apply(rgbBuffer);
        return spatial.apply(yBuffer);
    }

    @Override
    public void close() throws Exception {
        spatial.close();
    }
}
