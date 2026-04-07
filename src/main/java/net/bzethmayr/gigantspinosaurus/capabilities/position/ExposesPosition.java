package net.bzethmayr.gigantspinosaurus.capabilities.position;

import net.bzethmayr.gigantspinosaurus.model.framing.North;

public interface ExposesPosition {
    double DNLat();
    double DELong();
    double MUp();
    North north();
}
