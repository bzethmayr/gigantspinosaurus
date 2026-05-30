package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.gpu.GpuProgram;
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

    private final GpuProgram downsampleProg;
    private final GpuProgram dwtProg;
    private final GpuProgram sobelProg;

    private final GpuBuffer inputBuf;
    private final GpuBuffer yBuf;
    private final GpuBuffer dwtOut;
    private final GpuBuffer sobelOut;
    private final ClosingChain close;

    private static final int INPUT_BUF_BYTES = DOWNSAMPLE_WIDTH * DOWNSAMPLE_HEIGHT * Integer.BYTES;
    private static final int OUTPUT_BUF_BYTES = SOBEL_OUTPUT_CELLS * Integer.BYTES;

    public VideoPipeline(final GpuContext context, final int inWidth, final int inHeight) {
        this.context = context;
        this.inWidth = inWidth;
        this.inHeight = inHeight;

        ClosingChain chain = null;
        try {
            this.downsampleProg = context.loadProgram(getClass(), "/spv/downsample.spv", 2, 16);
            chain = new ClosingChain(downsampleProg);
            this.dwtProg = context.loadProgram(getClass(), "/spv/dwt_ll.spv", 2, 12);
            chain = chain.link(dwtProg);
            this.sobelProg = context.loadProgram(getClass(), "/spv/sobel_feature.spv", 2, 12);
            chain = chain.link(sobelProg);
            this.inputBuf = createStorageBuf((long) inWidth * inHeight * Integer.BYTES);
            chain = chain.link(inputBuf);
            this.yBuf = createStorageBuf(INPUT_BUF_BYTES);
            chain = chain.link(yBuf);
            this.dwtOut = createStorageBuf(OUTPUT_BUF_BYTES);
            chain = chain.link(dwtOut);
            this.sobelOut = createStorageBuf(OUTPUT_BUF_BYTES);
            chain = chain.link(sobelOut);
        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        close = chain;
    }

    private GpuBuffer createStorageBuf(final long size) {
        return context.createBuffer(new GpuBuffer.BufferDesc(size,
                GpuBuffer.BufferUsage.STORAGE, GpuBuffer.MemoryHint.CPU_VISIBLE));
    }

    public ByteBuffer execute(final ByteBuffer yBuffer) {
        inputBuf.upload(0, yBuffer);

        context.asJob(spec -> {
            spec.stage(downsampleProg, loan ->
                    loan.bindBuffer(0, inputBuf)
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
        });

        final var result = ByteBuffer.allocate(REDUCED_OUTPUT_BYTES);
        result.put(packOutput(dwtOut, DWT_OUTPUT_CELLS));
        result.put(packOutput(sobelOut, SOBEL_OUTPUT_CELLS));
        result.flip();
        return result;
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

    private byte[] packOutput(final GpuBuffer srcBuf, final int numCells) {
        final int bufBytes = numCells * Integer.BYTES;
        final var raw = context.exchangeBuffer(bufBytes);
        srcBuf.download(0, raw);
        raw.rewind();
        final var packed = new byte[numCells];
        for (int i = 0; i < numCells; i++) {
            packed[i] = raw.get(i * Integer.BYTES);
        }
        return packed;
    }

    @Override
    public void close() throws Exception {
        close.close();
    }
}
