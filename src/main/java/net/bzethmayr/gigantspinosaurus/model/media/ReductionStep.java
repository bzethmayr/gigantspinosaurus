package net.bzethmayr.gigantspinosaurus.model.media;


import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;

import java.nio.ByteBuffer;
import java.util.SequencedSet;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseUnsupported;

/**
 * Expressed as consecutive shorts.
 */
public record ReductionStep(short reductionId, short reductionVersion) implements HasCanonicalAttributes {
    public static final String NON_CANONICAL = "ReductionStep has non-canonical serialization";
    public static final short NO_REDUCTION = 0;
    public static final short FINAL_VERSION = -1;

    public static ReductionStep noStep() {
        return new ReductionStep(NO_REDUCTION, FINAL_VERSION);
    }

    @Override
    public SequencedSet<String> getCanonicalAttributes() {
        throw becauseUnsupported(NON_CANONICAL);
    }

    @Override
    public short version() {
        throw becauseUnsupported(NON_CANONICAL);
    }

    @Override
    public byte[] getAttributeValue(String attributeName) {
        throw becauseUnsupported(NON_CANONICAL);
    }

    @Override
    public byte[] canonicalBytes() {
        final ByteBuffer out = ByteBuffer.allocate(4);
        out.putShort(reductionId);
        out.putShort(reductionVersion);
        return out.array();
    }
}
