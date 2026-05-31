package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.gpu.GpuProgram;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;

import java.nio.ByteBuffer;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.*;

public class VideoPipeline implements AutoCloseable {
    private static final int DOWNSAMPLE_GROUPS_X = (DOWNSAMPLE_WIDTH + 7) / 8;
    private static final int DOWNSAMPLE_GROUPS_Y = (DOWNSAMPLE_HEIGHT + 7) / 8;

    private final GpuContext context;
    private final int inWidth;
    private final int inHeight;
    private final boolean hasYcbcr;
    private final ReductionStep[] activeSteps;

    private final GpuProgram ycbcrProg;
    private final GpuProgram downsampleProg;
    private final GpuProgram dwtProg;
    private final GpuProgram sobelProg;
    private final GpuProgram packingProg;

    private final GpuBuffer rgbBuf;
    private final GpuBuffer yFullBuf;
    private final GpuBuffer yBuf;
    private final GpuBuffer dwtOut;
    private final GpuBuffer sobelOut;
    private final GpuBuffer packedOut;
    private final ClosingChain close;

    private static final int INPUT_BUF_BYTES = DOWNSAMPLE_WIDTH * DOWNSAMPLE_HEIGHT * Integer.BYTES;
    private static final int OUTPUT_BUF_BYTES = SOBEL_OUTPUT_CELLS * Integer.BYTES;

    public VideoPipeline(final GpuContext context, final int inWidth, final int inHeight,
                         final ReductionStep... optionalSteps) {
        this.context = context;
        this.inWidth = inWidth;
        this.inHeight = inHeight;

        boolean hasYcbcr = false;
        short ycbcrVersion = YCBCR_VERSION;
        short spatialVersion = SPATIAL_VERSION;
        for (final ReductionStep step : optionalSteps) {
            if (step.reductionId() == YCBCR_ID) {
                hasYcbcr = true;
                ycbcrVersion = step.reductionVersion();
            }
            if (step.reductionId() == SPATIAL_ID) {
                spatialVersion = step.reductionVersion();
            }
        }
        this.hasYcbcr = hasYcbcr;

        if (hasYcbcr) {
            this.activeSteps = new ReductionStep[]{
                    new ReductionStep(YCBCR_ID, ycbcrVersion),
                    new ReductionStep(SPATIAL_ID, spatialVersion)
            };
        } else {
            this.activeSteps = new ReductionStep[]{
                    new ReductionStep(SPATIAL_ID, spatialVersion)
            };
        }

        ClosingChain chain = null;
        try {
            this.ycbcrProg = context.loadProgram(
                    getClass(), "/spv/ycbcr_reduction.spv", 2, 8);
            chain = new ClosingChain(ycbcrProg);
            this.downsampleProg = context.loadProgram(
                    getClass(), "/spv/downsample.spv", 2, 16);
            chain = chain.link(downsampleProg);
            this.dwtProg = context.loadProgram(
                    getClass(), "/spv/dwt_ll.spv", 2, 12);
            chain = chain.link(dwtProg);
            this.sobelProg = context.loadProgram(
                    getClass(), "/spv/sobel_feature.spv", 2, 12);
            chain = chain.link(sobelProg);
            this.packingProg = context.loadProgram(
                    getClass(), "/spv/cell_packing.spv", 3, 4);
            chain = chain.link(packingProg);
            this.rgbBuf = createStorageBuf((long) inWidth * inHeight * Integer.BYTES);
            chain = chain.link(rgbBuf);
            this.yFullBuf = createStorageBuf((long) inWidth * inHeight * Integer.BYTES);
            chain = chain.link(yFullBuf);
            this.yBuf = createStorageBuf(INPUT_BUF_BYTES);
            chain = chain.link(yBuf);
            this.dwtOut = createStorageBuf(OUTPUT_BUF_BYTES);
            chain = chain.link(dwtOut);
            this.sobelOut = createStorageBuf(OUTPUT_BUF_BYTES);
            chain = chain.link(sobelOut);
            this.packedOut = createStorageBuf(REDUCED_OUTPUT_BYTES);
            chain = chain.link(packedOut);
        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        close = chain;
    }

    public ReductionStep[] reductionSteps() {
        return activeSteps;
    }

    private GpuBuffer createStorageBuf(final long size) {
        return context.createBuffer(new GpuBuffer.BufferDesc(size,
                GpuBuffer.BufferUsage.STORAGE, GpuBuffer.MemoryHint.CPU_VISIBLE));
    }

    public ByteBuffer execute(final ByteBuffer inputBuffer) {
        final int pixels = inWidth * inHeight;

        if (hasYcbcr) {
            inputBuffer.rewind();
            rgbBuf.upload(0, inputBuffer);
        } else {
            inputBuffer.rewind();
            yFullBuf.upload(0, inputBuffer);
        }

        final int ycbcrGroupsX = (inWidth + 7) / 8;
        final int ycbcrGroupsY = (inHeight + 7) / 8;

        context.asJob(spec -> {
            if (hasYcbcr) {
                spec.stage(ycbcrProg, loan ->
                        loan.bindBuffer(0, rgbBuf)
                                .bindBuffer(1, yFullBuf)
                                .setScalars(ycbcrPush())
                                .dispatch(ycbcrGroupsX, ycbcrGroupsY, 1));
                spec.barrier();
            }
            spec.stage(downsampleProg, loan ->
                    loan.bindBuffer(0, yFullBuf)
                            .bindBuffer(1, yBuf)
                            .setScalars(downsamplePush())
                            .dispatch(DOWNSAMPLE_GROUPS_X, DOWNSAMPLE_GROUPS_Y, 1));
            spec.barrier();
            spec.stage(dwtProg, loan ->
                    loan.bindBuffer(0, yBuf)
                            .bindBuffer(1, dwtOut)
                            .setScalars(dwtPush())
                            .dispatch(1, 1, 1));
            spec.stage(sobelProg, loan ->
                    loan.bindBuffer(0, yBuf)
                            .bindBuffer(1, sobelOut)
                            .setScalars(sobelPush())
                            .dispatch(1, 1, 1));
            spec.barrier();
            spec.stage(packingProg, loan ->
                    loan.bindBuffer(0, dwtOut)
                            .bindBuffer(1, sobelOut)
                            .bindBuffer(2, packedOut)
                            .setScalars(packPush())
                            .dispatch(1, 1, 1));
        });

        final var result = context.exchangeBuffer(REDUCED_OUTPUT_BYTES);
        packedOut.download(0, result);
        result.rewind();
        return result;
    }

    private ByteBuffer ycbcrPush() {
        final var buf = context.exchangeBuffer(8);
        buf.putInt(0, inWidth);
        buf.putInt(4, inHeight);
        return buf;
    }

    private ByteBuffer downsamplePush() {
        final var buf = context.exchangeBuffer(16);
        buf.putInt(0, inWidth);
        buf.putInt(4, inHeight);
        buf.putInt(8, DOWNSAMPLE_WIDTH);
        buf.putInt(12, DOWNSAMPLE_HEIGHT);
        return buf;
    }

    private ByteBuffer dwtPush() {
        final var buf = context.exchangeBuffer(12);
        buf.putInt(0, DOWNSAMPLE_WIDTH);
        buf.putInt(4, DOWNSAMPLE_HEIGHT);
        buf.putInt(8, 2);
        return buf;
    }

    private ByteBuffer sobelPush() {
        final var buf = context.exchangeBuffer(12);
        buf.putInt(0, DOWNSAMPLE_WIDTH);
        buf.putInt(4, DOWNSAMPLE_HEIGHT);
        buf.putInt(8, 4);
        return buf;
    }

    private ByteBuffer packPush() {
        final var buf = context.exchangeBuffer(4);
        buf.putInt(0, DWT_OUTPUT_CELLS);
        return buf;
    }

    @Override
    public void close() throws Exception {
        close.close();
    }
}
