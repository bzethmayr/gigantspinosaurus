package net.bzethmayr.gigantspinosaurus.model;

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
) implements HasRequiredAttributes {
    Orientation(double QW, double QX, double QY, double QZ) {
        this(QW, QX, QY, QZ, new Frame());
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
