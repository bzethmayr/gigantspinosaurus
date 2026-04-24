package net.bzethmayr.gigantspinosaurus.model.media;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import static net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.MAR_VERSION;
import static net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia.*;

@FunctionalInterface
public interface CreatesMedia<T extends ExposesMedia> {

    T createMedia(
            ReductionStep r0,
            ReductionStep r1,
            ReductionStep r2,
            ReductionStep r3,
            byte[] BLK3,
            short version);

    default T copyMedia(final ExposesMedia media) {
        return createMedia(media.r0(), media.r1(), media.r2(), media.r3(), media.BLK3(), media.version());
    }

    static ReductionStep readReductionStep(final ByteBuffer in) {
        return new ReductionStep(in.getShort(), in.getShort());
    }

    static <T extends ExposesMedia> HasCanonicalAttributes.CanonicalDecoder<T> decodesMedias(final CreatesMedia<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            ReductionStep r0 = null;
            ReductionStep r1 = null;
            ReductionStep r2 = null;
            ReductionStep r3 = null;
            byte[] mediaBLK3 = new byte[MEDIA_HASH_BYTES];
            short version = MAR_VERSION;

            final Set<String> keys = new HashSet<>();
            do {
                String key = readAsciiKey(in);
                requireKeyUnique(keys, key);
                expect(in, VAL);

                switch (key) {
                    case REDUCTION_0_FIELD -> r0 = readReductionStep(in);
                    case REDUCTION_1_FIELD -> r1 = readReductionStep(in);
                    case REDUCTION_2_FIELD -> r2 = readReductionStep(in);
                    case REDUCTION_3_FIELD -> r3 = readReductionStep(in);
                    case MEDIA_HASH_FIELD -> in.get(mediaBLK3);
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }
            } while (!checkSep(in));

            return ctor.createMedia(
                    r0, r1, r2, r3, mediaBLK3, version);
        };
    }
}
