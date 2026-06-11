package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.MinimalAttestationRecord;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMarks;
import net.bzethmayr.gigantspinosaurus.usage.BindsExtractionPipeline;
import net.bzethmayr.gigantspinosaurus.usage.MarDecoding;
import net.zethmayr.fungu.core.declarations.SingleUse;

import java.nio.ByteBuffer;
import java.util.Optional;

@SingleUse
public class VideoVerification {
    private final BindsExtractionPipeline extraction;
    private final ExtractsMar decoder;
    private ExtractsMarks extractor;
    private int lastDecodedIndex;


    public VideoVerification(final BindsExtractionPipeline extraction) {
        this.extraction = extraction;
        this.extractor = extraction.extractorFactory().get();
        this.decoder = extraction.decoder();
        this.lastDecodedIndex = Integer.MIN_VALUE;
    }

    public Optional<byte[]> acceptFrame(final ByteBuffer frame, final int frameIndex) {
        final ByteBuffer mask = extractor.apply(frame);
        if (!mask.hasRemaining()) return Optional.empty();

        final byte[] marBytes = decoder.apply(mask);
        if (marBytes.length == 0) return Optional.empty();

        final MinimalAttestationRecord mar = MarDecoding.decode(ByteBuffer.wrap(marBytes));
        if (mar.index() <= lastDecodedIndex) return Optional.empty();

        lastDecodedIndex = mar.index();
        return Optional.of(marBytes);
    }

    public Optional<byte[]> flush() {
        return Optional.empty();
    }

    public void reset() {
        extractor = extraction.extractorFactory().get();
        lastDecodedIndex = Integer.MIN_VALUE;
    }
}
