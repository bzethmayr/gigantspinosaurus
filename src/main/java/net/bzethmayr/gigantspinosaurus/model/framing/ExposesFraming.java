package net.bzethmayr.gigantspinosaurus.model.framing;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromEnum;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesFraming extends HasRequiredAttributes {
    short FRAMING_VERSION = 3;
    String FRAME_FIELD = "frame";
    String HORZ_FIELD = "x";
    String VERT_FIELD = "y";
    String FACE_FIELD = "z";
    String HAND_FIELD = "handed";

    Handedness x();
    Vertical y();
    Face z();
    Handedness handed();
    North north();

    BoundAttributes<ExposesFraming> ACCESSORS = new BoundAttributes<>(
            adds("x", fromEnum(ExposesFraming::x)),
            adds("y", fromEnum(ExposesFraming::y)),
            adds("z", fromEnum(ExposesFraming::z)),
            adds("handed", fromEnum(ExposesFraming::handed)),
            adds("north", fromEnum(ExposesFraming::north)),
            Versioned.addsVersion()
    );

    @Override
    default short version() {
        return FRAMING_VERSION;
    }

    @Override
    default SequencedSet<String> getRequiredAttributes() {
        return ACCESSORS.fieldNames();
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
