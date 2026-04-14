package net.bzethmayr.gigantspinosaurus.usage.desktop;

import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;

import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;

public class DesktopPosition implements ExposesPosition {

    @Override
    public double DNLat() {
        return 0d;
    }

    @Override
    public double DELong() {
        return 0d;
    }

    @Override
    public double MUp() {
        return 0d;
    }

    @Override
    public North north() {
        return U_NORTH;
    }

    @Override
    public short version() {
        return POSITION_VERSION;
    }
}
