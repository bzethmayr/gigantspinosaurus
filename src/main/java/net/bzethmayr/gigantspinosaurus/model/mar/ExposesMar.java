package net.bzethmayr.gigantspinosaurus.model.mar;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesMar extends HasRequiredAttributes {
    String MAR_FIELD = "mar";
    String NONCE_FIELD = "nonce";
    String INDEX_FIELD = "index";
    String PRIOR_HASH_FIELD = "priorSH4_8";
    String TIME_FIELD = "utcEpochSeconds";
    String POSITION_FIELD = "position";
    String ORIENTATION_FIELD = "orientation";
    String CURRENT_HASH_FIELD = "currentSH4_8";
    String SIGNATURE_FIELD = "signature";

    short MAR_VERSION = 2;
    long nonce();
    int index();
    long priorSH4_8();
    double utcEpochSeconds();
    ExposesPosition position();
    ExposesOrientation<?> orientation();
    long currentSH4_8();
    ExposesSignature signature();

    BoundAttributes<ExposesMar> ACCESSORS = new BoundAttributes<>(
            adds(NONCE_FIELD, fromLong(ExposesMar::nonce)),
            adds(INDEX_FIELD, fromInt(ExposesMar::index)),
            adds(PRIOR_HASH_FIELD, fromLong(ExposesMar::priorSH4_8)),
            adds(TIME_FIELD, fromDouble(ExposesMar::utcEpochSeconds)),
            adds(POSITION_FIELD, fromConverted(ExposesMar::position, ExposesPosition::canonicalBytes)),
            adds(ORIENTATION_FIELD, fromConverted(ExposesMar::orientation, ExposesOrientation::canonicalBytes)),
            adds(CURRENT_HASH_FIELD, fromLong(ExposesMar::currentSH4_8)),
            adds(SIGNATURE_FIELD, fromConverted(ExposesMar::signature, ExposesSignature::canonicalBytes)),
            Versioned.addsVersion()
    );

    @Override
    default short version() {
        return MAR_VERSION;
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
