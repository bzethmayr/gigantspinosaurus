package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.capabilities.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.framing.North;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromEnum;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record Geoposition(double DNLat, double DELong, double MUp, North north, short version)
        implements HasRequiredAttributes, ExposesPosition {

    Geoposition(double DNLat, double DELong) {
        this(DNLat, DELong, Double.NaN);
    }
    Geoposition(double DNLat, double DELong, North north) {
        this(DNLat, DELong, Double.NaN, north, (short) 0);
    }
    Geoposition(double DNLat, double DELong, double MUp) {
        this(DNLat, DELong, MUp, North.TRUE, (short) 0);
    }

    private static final BoundAttributes<Geoposition> ACCESSORS = new BoundAttributes<>(
            adds("DNLat", fromDouble(Geoposition::DNLat)),
            adds("DELong", fromDouble(Geoposition::DELong)),
            adds("MUp", fromDouble(Geoposition::MUp)),
            adds("north", fromEnum(Geoposition::north)),
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
