package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.ExposesQuaternion;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromConverted;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Orientation(
        double QW,
        double QX,
        double QY,
        double QZ,
        Frame frame
) implements HasRequiredAttributes, ExposesQuaternion {
    Orientation(double QW, double QX, double QY, double QZ) {
        this(QW, QX, QY, QZ, new Frame());
    }
    Orientation(double[] q4) {
        this(q4[0], q4[1], q4[2], q4[3]);
    }
    Orientation(double[] q4, Frame frame) {
        this(q4[0], q4[1], q4[2], q4[3], frame);
    }

    private static final BoundAttributes<Orientation> ACCESSORS = new BoundAttributes<>(
            adds("QW", fromDouble(Orientation::QW)),
            adds("QX", fromDouble(Orientation::QX)),
            adds("QY", fromDouble(Orientation::QY)),
            adds("QZ", fromDouble(Orientation::QZ)),
            adds("frame", fromConverted(Orientation::frame, f -> null))
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
