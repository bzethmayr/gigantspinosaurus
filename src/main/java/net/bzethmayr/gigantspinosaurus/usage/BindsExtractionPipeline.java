package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMarks;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A mark extraction and decoding pipeline.
 * @param extractorFactory provides stateful mark extractors per media stream
 * @param decoder the mark decoder, stateless
 */
public record BindsExtractionPipeline(
        Supplier<ExtractsMarks> extractorFactory,
        ExtractsMar decoder) {

    /**
     * One-shot extraction and decode. Creates a new extractor per call — suitable
     * only when no rolling-buffer state is needed across frames.
     */
    public Optional<byte[]> extractAndDecode(final ByteBuffer markedFrame) {
        final ExtractsMarks extractor = extractorFactory.get();
        final ByteBuffer mask = extractor.apply(markedFrame);
        if (!mask.hasRemaining()) return Optional.empty();
        final byte[] marBytes = decoder.apply(mask);
        return marBytes.length == 0 ? Optional.empty() : Optional.of(marBytes);
    }
}
