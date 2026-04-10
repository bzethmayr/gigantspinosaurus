package net.bzethmayr.gigantspinosaurus.model.framing;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonicalDecoder;
import net.bzethmayr.gigantspinosaurus.model.Framing;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.VAL;
import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.becauseBadKey;
import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.checkSep;
import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.expect;
import static net.bzethmayr.gigantspinosaurus.model.decoding.DecoderHelper.readAsciiKey;
import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.U_HAND;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.U_VERT;

@FunctionalInterface
public interface CreatesFraming<T extends ExposesFraming> {
    T createFraming(Handedness x,
                          Vertical y,
                          Face z,
                          Handedness handed,
                          North north,
                          short version);

    static <T extends ExposesFraming> CanonicalDecoder<T> createsFramings(final CreatesFraming<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            Handedness x = U_HAND;
            Vertical y = U_VERT;
            Face z = U_FACE;
            Handedness handedness = U_HAND;
            North north = U_NORTH;
            short version = FRAMING_VERSION;

            while (true) {
                String key = readAsciiKey(in);
                expect(in, VAL);

                switch (key) {
                    case HORZ_FIELD -> x = Handedness.valueOf(readAsciiKey(in));
                    case VERT_FIELD -> y = Vertical.valueOf(readAsciiKey(in));
                    case FACE_FIELD -> z = Face.valueOf(readAsciiKey(in));
                    case HAND_FIELD -> handedness = Handedness.valueOf(readAsciiKey(in));
                    case NORTH_FIELD -> north = North.valueOf(readAsciiKey(in));
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }

                if (checkSep(in)) break;
            }

            return ctor.createFraming(x, y, z, handedness, north, version);
        };
    }
}
