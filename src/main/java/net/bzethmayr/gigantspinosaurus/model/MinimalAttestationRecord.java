package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;

import java.util.Map;
import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record MinimalAttestationRecord(
        long nonce,
        double utcEpochSeconds,
        Geoposition position,
        Orientation orientation,
        Map<String, byte[]> alsoAttested,
        MarSignature signature
) implements HasRequiredAttributes {
    private static final BoundAttributes<MinimalAttestationRecord> ACCESSORS = new BoundAttributes<>(
                    adds("nonce", fromLong(MinimalAttestationRecord::nonce)),
                    adds("utcEpochSeconds", fromDouble(MinimalAttestationRecord::utcEpochSeconds)),
                    adds("position", fromConverted(MinimalAttestationRecord::position, p -> null)),
                    adds("orientation", fromConverted(MinimalAttestationRecord::orientation, o -> null)),
                    adds("signature", fromConverted(MinimalAttestationRecord::signature, o -> null))
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
        return ACCESSORS.getBoundValueOrDelegate(attributeName, this, alsoAttested::get);
    }
}
