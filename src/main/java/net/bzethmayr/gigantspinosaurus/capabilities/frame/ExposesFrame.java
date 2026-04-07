package net.bzethmayr.gigantspinosaurus.capabilities.frame;

import net.bzethmayr.gigantspinosaurus.model.framing.Face;
import net.bzethmayr.gigantspinosaurus.model.framing.Handedness;
import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.framing.Vertical;

public interface ExposesFrame {
    Handedness x();
    Vertical y();
    Face z();
    Handedness handed();
    North north();
}
