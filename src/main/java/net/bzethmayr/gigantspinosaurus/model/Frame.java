package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromEnum;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromInt;
import static net.bzethmayr.gigantspinosaurus.model.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.Handedness.U_HAND;
import static net.bzethmayr.gigantspinosaurus.model.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.Vertical.U_VERT;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Frame(
        Handedness x,
        Vertical y,
        Face z,
        Handedness handed,
        North north,
        int version
) implements HasRequiredAttributes {
    Frame(Handedness x, Vertical y, Face z, Handedness handed, North north) {
        this(x, y, z, handed, north, 0);
    }

    Frame() {
        this(U_HAND, U_VERT, U_FACE, U_HAND, U_NORTH);
    }

    private static final BoundAttributes<Frame> ACCESSORS = new BoundAttributes<>(
            adds("x", fromEnum(Frame::x)),
            adds("y", fromEnum(Frame::y)),
            adds("z", fromEnum(Frame::z)),
            adds("handed", fromEnum(Frame::handed)),
            adds("north", fromEnum(Frame::north)),
            adds("version", fromInt(Frame::version))
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
