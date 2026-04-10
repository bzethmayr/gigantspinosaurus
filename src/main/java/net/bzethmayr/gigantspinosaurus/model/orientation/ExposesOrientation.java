package net.bzethmayr.gigantspinosaurus.model.orientation;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromConverted;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromDouble;
import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAME_FIELD;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesOrientation<R extends ExposesOrientation<R>> extends ExposesQuaternion<R>, HasCanonicalAttributes {
    short ORIENTATION_VERSION = 1;
    double QW();
    double QX();
    double QY();
    double QZ();
    ExposesFraming framing();

    BoundAttributes<ExposesOrientation<?>> ACCESSORS = new BoundAttributes<>(
            ExposesOrientation.class,
            Versioned.addsVersion(),
            adds(W_FIELD, fromDouble(ExposesOrientation::QW)),
            adds(X_FIELD, fromDouble(ExposesOrientation::QX)),
            adds(Y_FIELD, fromDouble(ExposesOrientation::QY)),
            adds(Z_FIELD, fromDouble(ExposesOrientation::QZ)),
            adds(FRAME_FIELD, fromConverted(ExposesOrientation::framing, ExposesFraming::canonicalBytes))
    );

    R withFraming(ExposesFraming framing);

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }

}
