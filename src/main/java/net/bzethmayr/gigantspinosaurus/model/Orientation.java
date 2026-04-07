package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.capabilities.orientation.ExposesQuaternion;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromConverted;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.model.Frame.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Orientation(
        double QW,
        double QX,
        double QY,
        double QZ,
        Frame frame,
        short version
) implements HasRequiredAttributes, ExposesQuaternion<Orientation> {

    public Orientation(double QW,
                       double QX,
                       double QY,
                       double QZ,
                       Frame frame) {
        this(QW, QX, QY, QZ, frame, (short) 0);
    }

    private static final BoundAttributes<Orientation> ACCESSORS = new BoundAttributes<>(
            adds("QW", fromDouble(Orientation::QW)),
            adds("QX", fromDouble(Orientation::QX)),
            adds("QY", fromDouble(Orientation::QY)),
            adds("QZ", fromDouble(Orientation::QZ)),
            adds("frame", fromConverted(Orientation::frame, Frame::canonicalBytes)),
            Versioned.addsVersion()
    );
    private static final SequencedSet<String> REQUIRED = ACCESSORS.fieldNamesExcept(VERSION_FIELD, FRAME_FIELD);

    public Orientation withQ4(final double[] q4) {
        return new Orientation(q4[0], q4[1], q4[2], q4[3], frame, (short) 0);
    }

    public Orientation withFrame(final Frame obtained) {
        return new Orientation(QW, QX, QY, QZ, obtained, (short) 0);
    }

    @Override
    public SequencedSet<String> getRequiredAttributes() {
        return REQUIRED;
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
