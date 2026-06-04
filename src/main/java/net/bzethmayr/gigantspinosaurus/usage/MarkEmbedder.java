package net.bzethmayr.gigantspinosaurus.usage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class MarkEmbedder implements PreparesMark, MarksMedia {
    private static final int QR_VERSION = 18;
    private static final ErrorCorrectionLevel QR_ECC = ErrorCorrectionLevel.M;
    private static final int QR_MODULES = QR_VERSION * 4 + 17;
    private static final int MARK_SIZE = QR_MODULES * QR_MODULES;
    private static final Map<EncodeHintType, Object> HINTS;

    static {
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, QR_ECC);
        hints.put(EncodeHintType.QR_VERSION, QR_VERSION);
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
        HINTS = Collections.unmodifiableMap(hints);
    }

    private final QRCodeWriter encoder = new QRCodeWriter();

    @Override
    public ByteBuffer emptyMark(final int marCanonicalSize) {
        return ByteBuffer.allocateDirect(MARK_SIZE);
    }

    @Override
    public void accept(final byte[] marBytes, final ByteBuffer markBuffer) {
        markBuffer.clear();
        try {
            final String payload = new String(marBytes, StandardCharsets.ISO_8859_1);
            final BitMatrix matrix = encoder.encode(payload, BarcodeFormat.QR_CODE, QR_MODULES, QR_MODULES, HINTS);
            for (int y = 0; y < QR_MODULES; y++) {
                for (int x = 0; x < QR_MODULES; x++) {
                    markBuffer.put(matrix.get(x, y) ? (byte) 255 : (byte) 0);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("QR encoding failed", e);
        }
        markBuffer.flip();
    }

    @Override
    public void mark(final ByteBuffer mark, final ByteBuffer target) {
        final int markLen = Math.min(mark.remaining(), target.remaining());
        mark.rewind();
        target.rewind();
        for (int i = 0; i < markLen; i++) {
            final int mp = mark.get(i) & 0xFF;
            final int tp = target.get(i) & 0xFF;
            target.put(i, (byte) (tp ^ mp));
        }
    }
}