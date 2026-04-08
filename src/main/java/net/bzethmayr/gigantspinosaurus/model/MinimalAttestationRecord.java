package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.*;

public record MinimalAttestationRecord(
        long nonce,
        int index,
        long priorSH4_8,
        double utcEpochSeconds,
        ExposesPosition position,
        ExposesOrientation<?> orientation,
        long currentSH4_8,
        ExposesSignature signature,
        short version
) implements ExposesMar {
    public MinimalAttestationRecord(long nonce,
                                    int index,
                                    long priorSH4_8,
                                    double utcEpochSeconds,
                                    ExposesPosition position,
                                    ExposesOrientation<?> orientation,
                                    long currentSH4_8,
                                    ExposesSignature signature) {
        this(nonce, index, priorSH4_8, utcEpochSeconds, position, orientation, currentSH4_8, signature, MAR_VERSION);
    }

    public static MinimalAttestationRecord decode(final ByteBuffer in, final CanonizesDecoders decoders) {
        expect(in, OPEN);

        Long nonce = null;
        int index = 0;
        long priorSH4_8 = 0L;
        Double utcEpochSeconds = null;
        ExposesPosition position = null;
        ExposesOrientation<?> orientation = null;
        Long currentSH4_8 = null;
        ExposesSignature signature = null;
        short version = MAR_VERSION;

        while (true) {
            String key = readAsciiKey(in); // reads up to ':'
            expect(in, VAL);

            switch (key) {
                case NONCE_FIELD -> nonce = in.getLong();
                case INDEX_FIELD -> index = in.getInt();
                case PRIOR_HASH_FIELD -> priorSH4_8 = in.getLong();
                case TIME_FIELD -> utcEpochSeconds = in.getDouble();
                case POSITION_FIELD -> position =
                        decoders.<ExposesPosition>decoderFor(POSITION_FIELD).decode(in, decoders);
                case ORIENTATION_FIELD -> orientation =
                        decoders.<ExposesOrientation<?>>decoderFor(ORIENTATION_FIELD).decode(in, decoders);
                case CURRENT_HASH_FIELD -> currentSH4_8 = in.getLong();
                case SIGNATURE_FIELD -> signature =
                        decoders.<ExposesSignature>decoderFor(SIGNATURE_FIELD).decode(in, decoders);
                case VERSION_FIELD -> version = in.getShort();
            }

            byte sep = in.get();
            if (sep == CLOSE) break;
            if (sep != SEP) throw becauseBadSeparator(sep);
        }

        return new MinimalAttestationRecord(
                nonce, index, priorSH4_8, utcEpochSeconds, position, orientation, currentSH4_8, signature, version);

    }
}
