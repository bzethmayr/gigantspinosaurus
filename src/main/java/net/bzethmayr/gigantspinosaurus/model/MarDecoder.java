package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonicalDecoder;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonizesDecoders;
import net.zethmayr.fungu.core.declarations.NotDone;

import java.nio.ByteBuffer;
import java.util.Map;

import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.*;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

@NotDone
public class MarDecoder implements CanonicalDecoder<MinimalAttestationRecord> {

    private static final class CanonicalRegistry {
        static final Map<String, CanonicalDecoder<?>> DECODERS = Map.of(
                POSITION_FIELD, Geoposition::decode,
                ORIENTATION_FIELD, Orientation::decode,
                FRAME_FIELD, Framing::decode,
                SIGNATURE_FIELD, MarSignature::decode,
                MAR_FIELD, MinimalAttestationRecord::decode
        );

        @SuppressWarnings("unchecked")
        static <T> CanonicalDecoder<T> decoderFor(String key) {
            return (CanonicalDecoder<T>) DECODERS.get(key);
        }
    }

    public static CanonizesDecoders decoders() {
        return CanonicalRegistry::decoderFor;
    }

    public MinimalAttestationRecord decode(ByteBuffer in) {
        return decode(in, decoders());
    }

    @Override
    public MinimalAttestationRecord decode(ByteBuffer in, CanonizesDecoders decoders) {
        return MinimalAttestationRecord.decode(in, decoders);
    }

    public static void expect(final ByteBuffer in, final byte expected) {
        byte actual = in.get();
        if (actual != expected) {
            throw becauseIllegal("Unexpected character '%s'", actual);
        }
    }

    public static IllegalArgumentException becauseBadSeparator(final byte sep) {
        return becauseIllegal("Bad separator: %s", sep);
    }

    public static final byte OPEN = (byte) '{';
    public static final byte VAL = (byte) ':';
    public static final byte SEP = (byte) ',';
    public static final byte CLOSE = (byte) '}';

    private static boolean validForKey(final byte b) {
        return 1 == ((((b - 'A') | ('Z' - b)) >>> 31) ^ 1) +
                ((((b - '0') | ('9' - b)) >>> 31) ^ 1) +
                ((((b - 'a') | ('z' - b)) >>> 31) ^ 1) +
                ((((b - '_') | ('_' - b)) >>> 31) ^ 1);
    }

    public static String readAsciiKey(final ByteBuffer in) {
        final StringBuilder keyOut = new StringBuilder();
        while (true) {
            byte b = in.get();
            if (!validForKey(b)) break;
            keyOut.append(b);
        }
        in.position(in.position() - 1);
        return keyOut.toString();
    }
}
