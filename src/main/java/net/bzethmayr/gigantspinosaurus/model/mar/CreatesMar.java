package net.bzethmayr.gigantspinosaurus.model.mar;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.util.HashSet;
import java.util.Set;

import static net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.capabilities.Versioned.VERSION_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.*;

public interface CreatesMar<T extends ExposesMar> {

    T createMar(long nonce,
                int index,
                long priorSH4_8,
                double utcEpochSeconds,
                ExposesPosition position,
                ExposesOrientation<?> orientation,
                byte[] mediaBLK3,
                long currentSH4_8,
                ExposesSignature signature,
                short version);

    default T copyMar(final ExposesMar mar) {
        return createMar(
                mar.nonce(), mar.index(), mar.priorSipH4_8(), mar.utcEpochSeconds(), mar.position(), mar.orientation(),
                mar.mediaBLK3(), mar.currentSipH4_8(), mar.signature(), mar.version());
    }

    static <T extends ExposesMar> HasCanonicalAttributes.CanonicalDecoder<T> decodesMars(final CreatesMar<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            Long nonce = null;
            int index = 0;
            long priorSH4_8 = 0L;
            Double utcEpochSeconds = null;
            ExposesPosition position = null;
            ExposesOrientation<?> orientation = null;
            byte[] mediaBLK3 = new byte[32];
            Long currentSH4_8 = null;
            ExposesSignature signature = null;
            short version = MAR_VERSION;

            final Set<String> keys = new HashSet<>();
            while (true) {
                String key = readAsciiKey(in);
                requireKeyUnique(keys, key);
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
                    case MEDIA_HASH_FIELD -> in.get(mediaBLK3);
                    case CURRENT_HASH_FIELD -> currentSH4_8 = in.getLong();
                    case SIGNATURE_FIELD -> signature =
                            decoders.<ExposesSignature>decoderFor(SIGNATURE_FIELD).decode(in, decoders);
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }

                if (checkSep(in)) break;
            }

            return ctor.createMar(
                    nonce, index, priorSH4_8, utcEpochSeconds, position, orientation, mediaBLK3, currentSH4_8, signature, version);
        };
    }
}
