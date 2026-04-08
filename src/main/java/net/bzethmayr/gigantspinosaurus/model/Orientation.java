package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;

public record Orientation(
        double QW,
        double QX,
        double QY,
        double QZ,
        ExposesFraming framing,
        short version
) implements ExposesOrientation<Orientation> {

    public Orientation(double QW,
                       double QX,
                       double QY,
                       double QZ,
                       ExposesFraming framing) {
        this(QW, QX, QY, QZ, framing, ORIENTATION_VERSION);
    }

    public Orientation withQ4(final double[] q4) {
        return new Orientation(q4[0], q4[1], q4[2], q4[3], framing, ORIENTATION_VERSION);
    }

    public Orientation withFraming(final Framing obtained) {
        return new Orientation(QW, QX, QY, QZ, obtained, ORIENTATION_VERSION);
    }

    public static Orientation decode(final ByteBuffer in, final CanonizesDecoders decoders) {
        expect(in, OPEN);

        Double QW = null;
        Double QX = null;
        Double QY = null;
        Double QZ = null;
        ExposesFraming framing = null;
        short version = ORIENTATION_VERSION;

        while (true) {
            String key = readAsciiKey(in);
            expect(in, VAL);

            switch (key) {
                case W_FIELD -> QW = in.getDouble();
                case X_FIELD -> QX = in.getDouble();
                case Y_FIELD -> QY = in.getDouble();
                case Z_FIELD -> QZ = in.getDouble();
                case FRAME_FIELD -> framing =
                        decoders.<ExposesFraming>decoderFor(FRAME_FIELD).decode(in, decoders);
                case VERSION_FIELD -> version = in.getShort();
            }

            byte sep = in.get();
            if (sep == CLOSE) break;
            if (sep != SEP) throw becauseBadSeparator(sep);
        }

        return new Orientation(QW, QX, QY, QZ, framing, version);
    }
}
