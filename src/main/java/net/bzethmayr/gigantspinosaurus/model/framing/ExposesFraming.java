package net.bzethmayr.gigantspinosaurus.model.framing;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromEnum;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.NORTH_FIELD;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesFraming extends HasCanonicalAttributes {
    short FRAMING_VERSION = 4;
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
            ExposesFraming.class,
            Versioned.addsVersion(),
            adds(HORZ_FIELD, fromEnum(ExposesFraming::x)),
            adds(VERT_FIELD, fromEnum(ExposesFraming::y)),
            adds(FACE_FIELD, fromEnum(ExposesFraming::z)),
            adds(HAND_FIELD, fromEnum(ExposesFraming::handed)),
            adds(NORTH_FIELD, fromEnum(ExposesFraming::north))
    );

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }

}
