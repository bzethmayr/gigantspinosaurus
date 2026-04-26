package net.bzethmayr.gigantspinosaurus.model.media;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromBytes;
import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromConverted;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

public interface ExposesMedia extends HasCanonicalAttributes {
    String REDUCTION_0_FIELD = "r0";
    String REDUCTION_1_FIELD = "r1";
    String REDUCTION_2_FIELD = "r2";
    String REDUCTION_3_FIELD = "r3";
    String MEDIA_HASH_FIELD = "BLK3";

    int MAX_REDUCERS = 4;
    int MEDIA_HASH_BYTES = 32;
    short MEDIA_VERSION = 0;

    ReductionStep r0();
    ReductionStep r1();
    ReductionStep r2();
    ReductionStep r3();
    byte[] BLK3();

    BoundAttributes<ExposesMedia> ACCESSORS = new BoundAttributes<>(
            ExposesMedia.class,
            Versioned.addsVersion(),
            adds(REDUCTION_0_FIELD, fromConverted(ExposesMedia::r0, ReductionStep::canonicalBytes)),
            adds(REDUCTION_1_FIELD, fromConverted(ExposesMedia::r1, ReductionStep::canonicalBytes)),
            adds(REDUCTION_2_FIELD, fromConverted(ExposesMedia::r2, ReductionStep::canonicalBytes)),
            adds(REDUCTION_3_FIELD, fromConverted(ExposesMedia::r3, ReductionStep::canonicalBytes)),
            adds(MEDIA_HASH_FIELD, fromBytes(ExposesMedia::BLK3))
    );

    @Override
    default short version() {
        return MEDIA_VERSION;
    }

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }

    static IllegalArgumentException becauseTooManyReducers() {
        return becauseIllegal("Only %s reducers are supported", MAX_REDUCERS);
    }
}
