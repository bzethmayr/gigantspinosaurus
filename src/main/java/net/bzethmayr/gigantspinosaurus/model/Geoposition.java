package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;

public record Geoposition(double DNLat, double DELong, double MUp, North north, short version)
        implements ExposesPosition {

    Geoposition(double DNLat, double DELong) {
        this(DNLat, DELong, Double.NaN);
    }
    Geoposition(double DNLat, double DELong, North north) {
        this(DNLat, DELong, Double.NaN, north, POSITION_VERSION);
    }
    Geoposition(double DNLat, double DELong, double MUp) {
        this(DNLat, DELong, MUp, North.U_NORTH, POSITION_VERSION);
    }
}
