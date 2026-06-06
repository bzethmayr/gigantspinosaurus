package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.MarkEmbedder.QR_MODULES;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

public class QrSpatialMark implements MarksMedia {
    public enum TimSense { PLUS, MINUS }

    public static final int DEFAULT_LUMA_OFFSET = 3;
    public static final int DEFAULT_MARGIN = 4;

    private final int lumaOffset;
    private final int modulePixels;
    private final int offsetX;
    private final int offsetY;
    private final int frameWidth;
    private final int frameHeight;

    public QrSpatialMark(
            final int lumaOffset,
            final int modulePixels,
            final int offsetX,
            final int offsetY,
            final int frameWidth,
            final int frameHeight
    ) {
        if (modulePixels < 1) throw becauseIllegal("modulePixels must be >= 1");
        if (lumaOffset < 1) throw becauseIllegal("lumaOffset must be >= 1");
        if (offsetX + QR_MODULES * modulePixels > frameWidth)
            throw becauseIllegal("QR extends beyond frame width");
        if (offsetY + QR_MODULES * modulePixels > frameHeight)
            throw becauseIllegal("QR extends beyond frame height");
        this.lumaOffset = lumaOffset;
        this.modulePixels = modulePixels;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public static int computeModulePixels(final int frameWidth, final int frameHeight) {
        final int usable = Math.min(frameWidth, frameHeight) - 2 * DEFAULT_MARGIN;
        return Math.max(1, usable / QR_MODULES);
    }

    public static QrSpatialMark autoFit(final int lumaOffset, final int frameWidth, final int frameHeight) {
        final int mp = computeModulePixels(frameWidth, frameHeight);
        final int ox = (frameWidth - QR_MODULES * mp) / 2;
        final int oy = (frameHeight - QR_MODULES * mp) / 2;
        return new QrSpatialMark(lumaOffset, mp, ox, oy, frameWidth, frameHeight);
    }

    private static TimSense senseForIndex(final int frameIndex) {
        return (frameIndex % 2 == 0) ? TimSense.PLUS : TimSense.MINUS;
    }

    @Override
    public void mark(final ByteBuffer mark, final ByteBuffer target, final int frameIndex) {
        final TimSense sense = senseForIndex(frameIndex);
        mark.rewind();
        target.rewind();

        final byte[] markBytes = new byte[mark.remaining()];
        mark.get(markBytes);

        for (int row = 0; row < QR_MODULES; row++) {
            for (int col = 0; col < QR_MODULES; col++) {
                final int moduleValue = markBytes[row * QR_MODULES + col] & 0xFF;
                if (moduleValue == 0) continue;

                final int px = offsetX + col * modulePixels;
                final int py = offsetY + row * modulePixels;

                for (int dy = 0; dy < modulePixels; dy++) {
                    for (int dx = 0; dx < modulePixels; dx++) {
                        final int pixelIndex = ((py + dy) * frameWidth + (px + dx)) * 4;
                        final int r = target.get(pixelIndex) & 0xFF;
                        final int g = target.get(pixelIndex + 1) & 0xFF;
                        final int b = target.get(pixelIndex + 2) & 0xFF;

                        final int clampedR = switch (sense) {
                            case PLUS -> Math.min(255, r + lumaOffset);
                            case MINUS -> Math.max(0, r - lumaOffset);
                        };
                        final int clampedG = switch (sense) {
                            case PLUS -> Math.min(255, g + lumaOffset);
                            case MINUS -> Math.max(0, g - lumaOffset);
                        };
                        final int clampedB = switch (sense) {
                            case PLUS -> Math.min(255, b + lumaOffset);
                            case MINUS -> Math.max(0, b - lumaOffset);
                        };

                        target.put(pixelIndex, (byte) clampedR);
                        target.put(pixelIndex + 1, (byte) clampedG);
                        target.put(pixelIndex + 2, (byte) clampedB);
                    }
                }
            }
        }
    }
}
