package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION;

public class ColorSpaceReduction implements ReducesMedia {
    private final int width;
    private final int height;
    private final ReductionStep[] steps;

    public ColorSpaceReduction(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.steps = new ReductionStep[]{new ReductionStep(YCBCR_ID, YCBCR_VERSION)};
    }

    @Override
    public ReductionStep[] reductions() {
        return steps;
    }

    @Override
    public ByteBuffer apply(final ByteBuffer rgbBuffer) {
        final int pixels = width * height;
        final ByteBuffer out = ByteBuffer.allocateDirect(pixels * Integer.BYTES);
        rgbBuffer.rewind();
        for (int i = 0; i < pixels; i++) {
            final int r = rgbBuffer.get() & 0xFF;
            final int g = rgbBuffer.get() & 0xFF;
            final int b = rgbBuffer.get() & 0xFF;
            rgbBuffer.get(); // slack byte
            final int y = (77 * r + 150 * g + 29 * b) >> 8;
            out.putInt(y);
        }
        out.flip();
        return out;
    }
}
