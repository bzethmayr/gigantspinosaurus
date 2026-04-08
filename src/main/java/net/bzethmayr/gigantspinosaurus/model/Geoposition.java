package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;

public record Geoposition(double DNLat, double DELong, double MUp, North north, short version)
        implements ExposesPosition {

    Geoposition(double DNLat, double DELong) {
        this(DNLat, DELong, Double.NaN);
    }
    Geoposition(double DNLat, double DELong, North north) {
        this(DNLat, DELong, Double.NaN, north, (short) 0);
    }
    Geoposition(double DNLat, double DELong, double MUp) {
        this(DNLat, DELong, MUp, North.U_NORTH, (short) 0);
    }

    public static Geoposition decode(final ByteBuffer in, final CanonizesDecoders decoders) {
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
            }

            byte sep = in.get();
            if (sep == CLOSE) break;
            if (sep != SEP) throw becauseBadSeparator(sep);
        }

        return new Geoposition(
                DNLat, DELong, MUp, north, version);
    }
}
