package net.bzethmayr.gigantspinosaurus.usage;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.media.ColorSpaceReduction;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation.ReducedFrameReceiver;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION;
import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class VideoMarringTimTest implements TestsWithBytes {

    private static final Path CROSS_FORMAT_DIR = Path.of("src/test/resources/cross-format");

    private static final class CpuReduction implements ReducesMedia {
        private final ColorSpaceReduction inner;
        private final ReductionStep[] steps;

        CpuReduction(final int width, final int height) {
            inner = new ColorSpaceReduction(width, height);
            steps = new ReductionStep[]{new ReductionStep(YCBCR_ID, YCBCR_VERSION)};
        }

        @Override
        public ReductionStep[] reductions() {
            return steps;
        }

        @Override
        public ByteBuffer apply(final ByteBuffer input) {
            return inner.apply(input);
        }
    }

    private static Stream<Path> losslessPngs() throws IOException {
        return Files.list(CROSS_FORMAT_DIR)
                .filter(p -> p.getFileName().toString().endsWith("_lossless.png"))
                .sorted();
    }

    @Test
    void zxingDirectBitMatrixDecode() throws Exception {
        // Encode a MAR, build a BitMatrix manually from mark bytes, decode
        final var ctors = defaultConstructors();
        final var env = desktopEnvironment();
        final var embedder = new MarkEmbedder();
        final var creation = new MarCreation(ctors, env);
        final var reduction = new CpuReduction(320, 240);
        final var receiver = creation.intentToRecord(reduction.reductions());
        final ByteBuffer reduced = reduction.apply(ByteBuffer.wrap(new byte[320 * 240 * 4]));
        final ExposesMar mar = receiver.reducedFrame(reduced, 0);
        final byte[] canonical = mar.canonicalBytes();

        final ByteBuffer markBuf = embedder.emptyMark(canonical.length);
        embedder.accept(canonical, markBuf);

        // Build a minimal binary image from the mark buffer, plus margins
        final int scale = 6;
        final int mod = MarkEmbedder.QR_MODULES;
        final int qrPx = mod * scale;
        final int margin = 10;
        final int W = qrPx + 2 * margin;
        final int H = qrPx + 2 * margin;

        final byte[] img = new byte[W * H * 4];
        // White background
        for (int i = 0; i < img.length; i += 4) {
            img[i] = (byte) 255;
            img[i + 1] = (byte) 255;
            img[i + 2] = (byte) 255;
        }
        // Render QR: black = 0, white = 255 in the mark buffer
        for (int row = 0; row < mod; row++) {
            for (int col = 0; col < mod; col++) {
                final int module = markBuf.get(row * mod + col) & 0xFF;
                if (module == 0) continue; // white module → keep background
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        final int px = (margin + row * scale + dy) * W + (margin + col * scale + dx);
                        final int off = px * 4;
                        img[off] = 0;
                        img[off + 1] = 0;
                        img[off + 2] = 0;
                    }
                }
            }
        }

        // Create ARGB int array for ZXing
        final int[] argb = new int[W * H];
        for (int i = 0; i < W * H; i++) {
            final int r = img[i * 4] & 0xFF;
            final int g = img[i * 4 + 1] & 0xFF;
            final int b = img[i * 4 + 2] & 0xFF;
            argb[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }

        final var source = new RGBLuminanceSource(W, H, argb);
        final var bitmap = new BinaryBitmap(new HybridBinarizer(source));
        final var reader = new QRCodeReader();
        final var result = reader.decode(bitmap);
        final byte[] decoded = result.getText().getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(canonical, decoded);
    }

    @Test
    void timAlternatingFrames_markExtractAndVerify() throws Exception {
        final Path imagePath = losslessPngs().findFirst().orElseThrow();
        final Raster raster = CrossFormatDecoder.decode(imagePath);

        final var ctors = defaultConstructors();
        final var env = desktopEnvironment();
        final var reduction = new CpuReduction(raster.width(), raster.height());
        final var embedder = new MarkEmbedder();
        final var creation = new MarCreation(ctors, env);

        // Create MAR from the original (unmarked) frame data
        final ReducedFrameReceiver receiver = creation.intentToRecord(reduction.reductions());
        final ByteBuffer cleanFrame = raster.toBuffer();
        final ByteBuffer reducedFrame0 = reduction.apply(cleanFrame);
        final ExposesMar mar0 = receiver.reducedFrame(reducedFrame0, 0);

        // Encode MAR[0] as QR mark buffer
        final ByteBuffer markBuffer = embedder.emptyMark(mar0.canonicalBytes().length);
        embedder.accept(mar0.canonicalBytes(), markBuffer);

        // Create spatial mark renderer with auto-fit parameters
        final var marker = QrSpatialMark.autoFit(
                QrSpatialMark.DEFAULT_LUMA_OFFSET, raster.width(), raster.height());

        // Create 8 individually marked frame copies (TIM alternates by frameIndex)
        final int frameCount = 10;
        final ByteBuffer[] markedFrames = new ByteBuffer[frameCount];
        final byte[] cleanRgb = raster.rgb();
        for (int i = 0; i < frameCount; i++) {
            final ByteBuffer frame = ByteBuffer.wrap(Arrays.copyOf(cleanRgb, cleanRgb.length));
            marker.mark(markBuffer, frame, i);
            markedFrames[i] = frame;
        }

        // Extraction with rolling buffer
        final var extractor = new RollingBufferExtractsMarks(
                5, 1, raster.width(), raster.height());
        final var qrDecoder = new ZxingDecodesMar(raster.width(), raster.height());
        final var verifier = new MarVerification(ctors, env);

        byte[] decodedMarBytes = null;
        for (int i = 0; i < frameCount; i++) {
            final ByteBuffer mask = extractor.apply(markedFrames[i]);
            if (!mask.hasRemaining()) continue;

            final byte[] candidate = qrDecoder.apply(mask);
            if (candidate.length > 0) {
                decodedMarBytes = candidate;
                break;
            }
        }

        assertNotNull(decodedMarBytes, "Should have decoded a MAR from marked frames");
        assertTrue(decodedMarBytes.length > 0, "Decoded MAR bytes should not be empty");

        final ExposesMar decodedMar = MarDecoding.decode(ByteBuffer.wrap(decodedMarBytes));

        assertEquals(mar0.index(), decodedMar.index(), "Decoded MAR index should match original");
        assertEquals(mar0.nonce(), decodedMar.nonce(), "Decoded MAR nonce should match original");
        assertEquals(mar0.priorSipH4_8(), decodedMar.priorSipH4_8(), "Decoded MAR prior hash should match");
        assertEquals(mar0.currentSipH4_8(), decodedMar.currentSipH4_8(), "Decoded MAR current hash should match");
        assertArrayEquals(mar0.media().BLK3(), decodedMar.media().BLK3(), "Decoded MAR BLAKE3 hash should match");

        assertTrue(verifier.verifyMedia(decodedMar, reducedFrame0),
                "Decoded MAR should cryptographically verify against original frame media");
    }
}
