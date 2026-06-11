package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMarks;

import java.nio.ByteBuffer;

/**
 * Since this is extraction, it is not in the _immediate_ critical optimization path.
 */
public class RollingBufferExtractsMarks implements ExtractsMarks {
    private final int bufferSize;
    private final int threshold;
    private final int frameWidth;
    private final int frameHeight;
    private final int pixelCount;

    private final int[] runningSum;
    private final byte[][] lumaBuffer;
    private int bufferPos;
    private int frameCount;

    public RollingBufferExtractsMarks(
            final int bufferSize,
            final int threshold,
            final int frameWidth,
            final int frameHeight
    ) {
        this.bufferSize = bufferSize;
        this.threshold = threshold;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.pixelCount = frameWidth * frameHeight;
        this.runningSum = new int[pixelCount];
        this.lumaBuffer = new byte[bufferSize][pixelCount];
        this.bufferPos = 0;
        this.frameCount = 0;
    }

    private static int luma(final ByteBuffer frame, final int pixelIndex) {
        // we have this as a shader too...
        final int r = frame.get(pixelIndex) & 0xFF;
        final int g = frame.get(pixelIndex + 1) & 0xFF;
        final int b = frame.get(pixelIndex + 2) & 0xFF;
        return (77 * r + 150 * g + 29 * b) >> 8;
    }

    @Override
    public ByteBuffer apply(final ByteBuffer frame) {
        frame.rewind();

        if (frameCount >= bufferSize) {
            final byte[] oldest = lumaBuffer[bufferPos];
            for (int i = 0; i < pixelCount; i++) {
                runningSum[i] -= oldest[i] & 0xFF;
            }
        }

        final byte[] current = lumaBuffer[bufferPos];
        for (int i = 0; i < pixelCount; i++) {
            final int y = luma(frame, i * 4);
            current[i] = (byte) y;
            runningSum[i] += y;
        }

        bufferPos = (bufferPos + 1) % bufferSize;
        if (frameCount < bufferSize) frameCount++;

        if (frameCount < bufferSize) {
            return ByteBuffer.allocate(0);
        }

        final ByteBuffer mask = ByteBuffer.allocate(pixelCount);
        for (int i = 0; i < pixelCount; i++) {
            final int avg = runningSum[i] / bufferSize;
            final int diff = Math.abs((lumaBuffer[(bufferPos - 1 + bufferSize) % bufferSize][i] & 0xFF) - avg);
            mask.put((byte) (diff > threshold ? 0 : (byte) 255));
        }
        mask.flip();
        return mask;
    }
}
