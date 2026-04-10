package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.*;

import static net.bzethmayr.gigantspinosaurus.model.framing.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.U_HAND;
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
        this(x, y, z, handed, north, FRAMING_VERSION);
    }

    public Framing() {
        this(U_HAND, U_VERT, U_FACE, U_HAND, U_NORTH);
    }
}
