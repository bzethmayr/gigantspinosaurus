package net.bzethmayr.gigantspinosaurus.capabilities.frame;

import net.bzethmayr.gigantspinosaurus.model.datum.Face;
import net.bzethmayr.gigantspinosaurus.model.datum.Handedness;
import net.bzethmayr.gigantspinosaurus.model.datum.North;
import net.bzethmayr.gigantspinosaurus.model.datum.Vertical;

public interface ExposesFrame {
    Handedness x();
    Vertical y();
    Face z();
    Handedness handed();
    North north();
}
