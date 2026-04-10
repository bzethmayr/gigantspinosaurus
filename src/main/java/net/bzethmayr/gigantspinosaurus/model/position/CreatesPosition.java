package net.bzethmayr.gigantspinosaurus.model.position;

import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes.CanonicalDecoder;
import net.bzethmayr.gigantspinosaurus.model.framing.North;

import static net.bzethmayr.gigantspinosaurus.capabilities.Versioned.VERSION_FIELD;
import static net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition.*;

@FunctionalInterface
public interface CreatesPosition<T extends ExposesPosition> {

    T createPosition(double DNLat, double DELong, double MUp, North north, short version);

    static <T extends ExposesPosition> CanonicalDecoder<T> createsPositions(final CreatesPosition<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            Double DNLat = null;
            Double DELong = null;
            double MUp = Double.NaN;
            North north = North.U_NORTH;
            short version = POSITION_VERSION;

            while (true) {
                String key = readAsciiKey(in);
                expect(in, VAL);

                switch (key) {
                    case LAT_FIELD -> DNLat = in.getDouble();
                    case LONG_FIELD -> DELong = in.getDouble();
                    case ELEV_FIELD -> MUp = in.getDouble();
                    case NORTH_FIELD -> north = North.valueOf(readAsciiKey(in));
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }

                if (checkSep(in)) break;
            }

            return ctor.createPosition(
                    DNLat, DELong, MUp, north, version);
        };
    }
}
