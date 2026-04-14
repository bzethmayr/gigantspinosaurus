package net.bzethmayr.gigantspinosaurus.usage.desktop;

import net.bzethmayr.gigantspinosaurus.model.Framing;
import net.bzethmayr.gigantspinosaurus.model.framing.*;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;

import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAMING_VERSION;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.FRONT;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.RIGHT;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.UP;

public class DesktopOrientation implements ExposesOrientation<DesktopOrientation> {
    private double w;
    private double x;
    private double y;
    private double z;

    public DesktopOrientation() {
        this(1, 0, 1, 0);
    }

    public DesktopOrientation(final double w, final double x, final double y, final double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double QW() {
        return w;
    }

    @Override
    public double QX() {
        return x;
    }

    @Override
    public double QY() {
        return y;
    }

    @Override
    public double QZ() {
        return z;
    }

    @Override
    public DesktopOrientation withQ4(double[] q4) {
        return new DesktopOrientation(q4[0], q4[1],q4[2],q4[3]);
    }

    @Override
    public ExposesFraming framing() {
        return new Framing(RIGHT, UP, FRONT, RIGHT, U_NORTH, FRAMING_VERSION);
    }

    @Override
    public DesktopOrientation withFraming(ExposesFraming framing) {
        return null;
    }

    @Override
    public short version() {
        return 0;
    }
}
