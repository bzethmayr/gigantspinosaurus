package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonicalDecoder;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonizesDecoders;
import net.bzethmayr.gigantspinosaurus.model.framing.CreatesFraming;
import net.bzethmayr.gigantspinosaurus.model.mar.CreatesMar;
import net.bzethmayr.gigantspinosaurus.model.orientation.CreatesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.CreatesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.CreatesSignature;

import java.nio.ByteBuffer;
import java.util.Map;

import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.*;

public final class MarDecoder {

    private static final CanonicalDecoder<Orientation> ORIENTATION_DECODER = CreatesOrientation.createsOrientations(Orientation::new);

    public static Orientation decodeOrientation(final ByteBuffer in, final CanonizesDecoders decoders) {
        return ORIENTATION_DECODER.decode(in, decoders);
    }

    private static final CanonicalDecoder<Geoposition> POSITION_DECODER = CreatesPosition.createsPositions(Geoposition::new);

    public static Geoposition decodePosition(final ByteBuffer in, final CanonizesDecoders decoders) {
        return POSITION_DECODER.decode(in, decoders);
    }

    private static final CanonicalDecoder<MarSignature> SIGNATURE_DECODER = CreatesSignature.createsSignatures(MarSignature::new);

    public static MarSignature decodeSignature(final ByteBuffer in, final CanonizesDecoders decoders) {
        return SIGNATURE_DECODER.decode(in, decoders);
    }

    private static final CanonicalDecoder<Framing> FRAMING_DECODER = CreatesFraming.createsFramings(Framing::new);

    public static Framing decodeFraming(final ByteBuffer in, final CanonizesDecoders decoders) {
        return FRAMING_DECODER.decode(in, decoders);
    }

    private static final CanonicalDecoder<MinimalAttestationRecord> MAR_DECODER = CreatesMar.createsMars(MinimalAttestationRecord::new);

    public static MinimalAttestationRecord decode(final ByteBuffer in, final CanonizesDecoders decoders) {
        return MAR_DECODER.decode(in, decoders);
    }

    public static MinimalAttestationRecord decode(final ByteBuffer in) {
        return decode(in, decoders());
    }

    private static final class CanonicalRegistry {
        static final Map<String, CanonicalDecoder<?>> DECODERS = Map.of(
                POSITION_FIELD, POSITION_DECODER,
                ORIENTATION_FIELD, ORIENTATION_DECODER,
                FRAME_FIELD, FRAMING_DECODER,
                SIGNATURE_FIELD, SIGNATURE_DECODER,
                MAR_FIELD, MAR_DECODER
        );

        @SuppressWarnings("unchecked")
        static <T> CanonicalDecoder<T> decoderFor(String key) {
            return (CanonicalDecoder<T>) DECODERS.get(key);
        }
    }

    public static CanonizesDecoders decoders() {
        return CanonicalRegistry::decoderFor;
    }
}
