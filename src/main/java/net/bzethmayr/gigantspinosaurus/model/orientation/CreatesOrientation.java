package net.bzethmayr.gigantspinosaurus.model.orientation;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonicalDecoder;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;

import java.util.HashSet;
import java.util.Set;

import static net.bzethmayr.gigantspinosaurus.capabilities.Versioned.VERSION_FIELD;
import static net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation.ORIENTATION_VERSION;
import static net.bzethmayr.gigantspinosaurus.model.orientation.ExposesQuaternion.*;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

@FunctionalInterface
public interface CreatesOrientation<T extends ExposesOrientation<T>> {

    T createOrientation(double QW,
                        double QX,
                        double QY,
                        double QZ,
                        ExposesFraming framing,
                        short version);

    default T copyOrientation(final ExposesOrientation<?> orientation) {
        return createOrientation(
                orientation.QW(),
                orientation.QX(),
                orientation.QY(),
                orientation.QZ(),
                orientation.framing(),
                orientation.version());
    }

    static <T extends ExposesOrientation<T>> CanonicalDecoder<T> decodesOrientations(final CreatesOrientation<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            Double QW = null;
            Double QX = null;
            Double QY = null;
            Double QZ = null;
            ExposesFraming framing = null;
            short version = ORIENTATION_VERSION;

            final Set<String> keys = new HashSet<>();
            while (true) {
                String key = readAsciiKey(in);
                if (!keys.add(key)) throw becauseIllegal("Duplicate key");
                expect(in, VAL);

                switch (key) {
                    case W_FIELD -> QW = in.getDouble();
                    case X_FIELD -> QX = in.getDouble();
                    case Y_FIELD -> QY = in.getDouble();
                    case Z_FIELD -> QZ = in.getDouble();
                    case FRAME_FIELD -> framing =
                            decoders.<ExposesFraming>decoderFor(FRAME_FIELD).decode(in, decoders);
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }

                if (checkSep(in)) break;
            }

            return ctor.createOrientation(QW, QX, QY, QZ, framing, version);
        };
    }
}
