package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;

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

    @Override
    public Orientation withFraming(final ExposesFraming obtained) {
        return new Orientation(QW, QX, QY, QZ, obtained, ORIENTATION_VERSION);
    }
}
