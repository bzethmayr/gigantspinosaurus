package net.bzethmayr.gigantspinosaurus.model.orientation;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromConverted;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesOrientation<R extends ExposesOrientation<R>> extends ExposesQuaternion<R>, HasRequiredAttributes {
    short ORIENTATION_VERSION = 0;
    double QW();
    double QX();
    double QY();
    double QZ();
    ExposesFraming framing();

    BoundAttributes<ExposesOrientation<?>> ACCESSORS = new BoundAttributes<>(
            adds("QW", fromDouble(ExposesOrientation::QW)),
            adds("QX", fromDouble(ExposesOrientation::QX)),
            adds("QY", fromDouble(ExposesOrientation::QY)),
            adds("QZ", fromDouble(ExposesOrientation::QZ)),
            adds("frame", fromConverted(ExposesOrientation::framing, ExposesFraming::canonicalBytes)),
            Versioned.addsVersion()
    );
    SequencedSet<String> REQUIRED = ACCESSORS.fieldNamesExcept(VERSION_FIELD, FRAME_FIELD);

    R withFraming(ExposesFraming framing);

    @Override
    default short version() {
        return ORIENTATION_VERSION;
    }

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
