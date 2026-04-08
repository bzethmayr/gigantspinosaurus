package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.*;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.U_HAND;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.U_VERT;

public record Framing(
        Handedness x,
        Vertical y,
        Face z,
        Handedness handed,
        North north,
        short version
) implements ExposesFraming {
    public Framing(Handedness x, Vertical y, Face z, Handedness handed, North north) {
        this(x, y, z, handed, north, (short) 0);
    }

    public Framing() {
        this(U_HAND, U_VERT, U_FACE, U_HAND, U_NORTH);
    }

    public static Framing decode(final ByteBuffer in, final CanonizesDecoders decoders) {
        expect(in, OPEN);

        Handedness x = U_HAND;
        Vertical y = U_VERT;
        Face z = U_FACE;
        Handedness handedness = U_HAND;
        North north = U_NORTH;
        short version = FRAMING_VERSION;

        while (true) {
            String key = readAsciiKey(in); // reads up to ':'
            expect(in, VAL);

            switch (key) {
                case HORZ_FIELD -> x = Handedness.valueOf(readAsciiKey(in));
                case VERT_FIELD -> y = Vertical.valueOf(readAsciiKey(in));
                case FACE_FIELD -> z = Face.valueOf(readAsciiKey(in));
                case HAND_FIELD -> handedness = Handedness.valueOf(readAsciiKey(in));
                case NORTH_FIELD -> north = North.valueOf(readAsciiKey(in));
                case VERSION_FIELD -> version = in.getShort();
            }

            byte sep = in.get();
            if (sep == CLOSE) break;
            if (sep != SEP) throw becauseBadSeparator(sep);
        }

        return new Framing(x, y, z, handedness, north, version);
    }
}
