package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.capabilities.frame.ExposesFrame;
import net.bzethmayr.gigantspinosaurus.model.framing.Face;
import net.bzethmayr.gigantspinosaurus.model.framing.Handedness;
import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.framing.Vertical;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.U_HAND;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.U_VERT;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Framing(
        Handedness x,
        Vertical y,
        Face z,
        Handedness handed,
        North north,
        short version
) implements HasRequiredAttributes, ExposesFrame {
    static final String FRAME_FIELD = "frame";
    public Framing(Handedness x, Vertical y, Face z, Handedness handed, North north) {
        this(x, y, z, handed, north, (short) 0);
    }

    public Framing() {
        this(U_HAND, U_VERT, U_FACE, U_HAND, U_NORTH);
    }

    private static final BoundAttributes<Framing> ACCESSORS = new BoundAttributes<>(
            adds("x", fromEnum(Framing::x)),
            adds("y", fromEnum(Framing::y)),
            adds("z", fromEnum(Framing::z)),
            adds("handed", fromEnum(Framing::handed)),
            adds("north", fromEnum(Framing::north)),
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
