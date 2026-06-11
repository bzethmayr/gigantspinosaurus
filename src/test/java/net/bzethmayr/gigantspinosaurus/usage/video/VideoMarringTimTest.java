package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation.ReducedFrameReceiver;
import net.bzethmayr.gigantspinosaurus.usage.MarDecoding;
import net.bzethmayr.gigantspinosaurus.usage.MarVerification;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder;
import net.bzethmayr.gigantspinosaurus.usage.images.CrossFormatDecoder.Raster;
import net.bzethmayr.gigantspinosaurus.usage.images.TestsWithImages;
import net.bzethmayr.gigantspinosaurus.usage.qr.QrMarkEmbedder;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static net.bzethmayr.gigantspinosaurus.usage.video.QrExtractionPipeline.videoTimExtraction;
import static org.junit.jupiter.api.Assertions.*;

class VideoMarringTimTest implements TestsWithBytes, TestsWithImages {

    @Test
    void timAlternatingFrames_markExtractAndVerify() throws Exception {
        final Path imagePath = TestsWithImages.losslessPngs().findFirst().orElseThrow();
        final Raster raster = CrossFormatDecoder.decode(imagePath);

        final var ctors = defaultConstructors();
        final var env = desktopEnvironment();
        final var reduction = new FakeReduction(raster.width(), raster.height());
        final var embedder = new QrMarkEmbedder(raster.width(), raster.height());
        final var creation = new MarCreation(ctors, env);

        // Create MAR from the original (unmarked) frame data
        final ReducedFrameReceiver receiver = creation.intentToRecord(reduction.reductions());
        final ByteBuffer cleanFrame = raster.toBuffer();
        final ByteBuffer reducedFrame0 = reduction.apply(cleanFrame);
        final ExposesMar mar0 = receiver.reducedFrame(reducedFrame0, 0);

        // Encode MAR[0] as QR mark buffer
        final ByteBuffer markBuffer = embedder.emptyMark(mar0.canonicalBytes().length);
        embedder.accept(mar0.canonicalBytes(), markBuffer);

        // Create 8 individually marked frame copies (TIM alternates by frameIndex)
        final int frameCount = 10;
        final ByteBuffer[] markedFrames = new ByteBuffer[frameCount];
        final byte[] cleanRgb = raster.rgb();
        for (int i = 0; i < frameCount; i++) {
            final ByteBuffer frame = ByteBuffer.wrap(Arrays.copyOf(cleanRgb, cleanRgb.length));
            embedder.mark(markBuffer, frame, i);
            markedFrames[i] = frame;
        }

        // Single-mark extraction with rolling buffer without mark cancellation (unmarred source frame)
        final var verifier = new MarVerification(ctors, env);
        final var videoVerification = new VideoVerification(videoTimExtraction(raster.width(), raster.height()));

        byte[] decodedMarBytes = null;
        for (int i = 0; i < frameCount; i++) {
            final Optional<byte[]> result = videoVerification.acceptFrame(markedFrames[i], i);
            if (result.isPresent()) {
                decodedMarBytes = result.get();
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
