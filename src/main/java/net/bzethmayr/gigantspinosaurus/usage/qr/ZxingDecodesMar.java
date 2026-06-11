package net.bzethmayr.gigantspinosaurus.usage.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMar;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ZxingDecodesMar implements ExtractsMar {
    private final int frameWidth;
    private final int frameHeight;
    private final QRCodeReader reader;

    public ZxingDecodesMar(final int frameWidth, final int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.reader = new QRCodeReader();
    }

    @Override
    public byte[] apply(final ByteBuffer mask) {
        if (mask.remaining() < frameWidth * frameHeight) {
            return new byte[0];
        }

        mask.rewind();
        // wants to go wide
        final int[] pixels = new int[frameWidth * frameHeight];
        for (int i = 0; i < pixels.length; i++) {
            final int v = mask.get(i) & 0xFF;
            pixels[i] = (0xFF << 24) | (v << 16) | (v << 8) | v;
        }

        final RGBLuminanceSource source = new RGBLuminanceSource(frameWidth, frameHeight, pixels);
        final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            final byte[] decoded = reader.decode(bitmap).getText().getBytes(StandardCharsets.ISO_8859_1);
            return decoded;
        } catch (final ReaderException e) {
            return new byte[0];
        }
    }
}
