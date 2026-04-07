package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.capabilities.frame.ExposesFrame;
import net.bzethmayr.gigantspinosaurus.model.datum.Face;
import net.bzethmayr.gigantspinosaurus.model.datum.Handedness;
import net.bzethmayr.gigantspinosaurus.model.datum.North;
import net.bzethmayr.gigantspinosaurus.model.datum.Vertical;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.model.datum.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.datum.Handedness.U_HAND;
import static net.bzethmayr.gigantspinosaurus.model.datum.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.datum.Vertical.U_VERT;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Frame(
        Handedness x,
        Vertical y,
        Face z,
        Handedness handed,
        North north,
        short version
) implements HasRequiredAttributes, ExposesFrame {
    static final String FRAME_FIELD = "frame";
    public Frame(Handedness x, Vertical y, Face z, Handedness handed, North north) {
        this(x, y, z, handed, north, (short) 0);
    }

    public Frame() {
        this(U_HAND, U_VERT, U_FACE, U_HAND, U_NORTH);
    }

    private static final BoundAttributes<Frame> ACCESSORS = new BoundAttributes<>(
            adds("x", fromEnum(Frame::x)),
            adds("y", fromEnum(Frame::y)),
            adds("z", fromEnum(Frame::z)),
            adds("handed", fromEnum(Frame::handed)),
            adds("north", fromEnum(Frame::north)),
            Versioned.addsVersion()
    );

    @Override
    public SequencedSet<String> getRequiredAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    public SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    public byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }
}
