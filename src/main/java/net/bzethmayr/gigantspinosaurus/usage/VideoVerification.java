package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.MinimalAttestationRecord;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMarks;

import java.nio.ByteBuffer;
import java.util.Optional;

public class VideoVerification {
    private final ExtractsMarks extractor;
    private final ExtractsMar decoder;
    private int lastDecodedIndex;

    public VideoVerification(final ExtractsMarks extractor, final ExtractsMar decoder) {
        this.extractor = extractor;
        this.decoder = decoder;
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
        lastDecodedIndex = Integer.MIN_VALUE;
    }
}
