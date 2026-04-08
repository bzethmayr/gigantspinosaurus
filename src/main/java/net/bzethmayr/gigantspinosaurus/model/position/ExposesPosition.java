package net.bzethmayr.gigantspinosaurus.model.position;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.model.framing.North;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromEnum;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesPosition extends HasRequiredAttributes {
    short POSITION_VERSION = 1;
    String LAT_FIELD = "DNLat";
    String LONG_FIELD = "DELong";
    String ELEV_FIELD = "MUp";

    double DNLat();
    double DELong();
    double MUp();
    North north();

    BoundAttributes<ExposesPosition> ACCESSORS = new BoundAttributes<>(
            adds(LAT_FIELD, fromDouble(ExposesPosition::DNLat)),
            adds(LONG_FIELD, fromDouble(ExposesPosition::DELong)),
            adds(ELEV_FIELD, fromDouble(ExposesPosition::MUp)),
            adds(NORTH_FIELD, fromEnum(ExposesPosition::north)),
            Versioned.addsVersion()
    );
    SequencedSet<String> REQUIRED = ACCESSORS.fieldNamesExcept(ELEV_FIELD, NORTH_FIELD);

    @Override
    default SequencedSet<String> getRequiredAttributes() {
        return REQUIRED;
    }

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }
}
