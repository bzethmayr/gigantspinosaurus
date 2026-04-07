package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record MinimalAttestationRecord(
        long nonce,
        int index,
        long priorSH4_8,
        double utcEpochSeconds,
        Geoposition position,
        Orientation orientation,
        long currentSH4_8,
        MarSignature signature,
        short version
) implements HasRequiredAttributes {
    private static final BoundAttributes<MinimalAttestationRecord> ACCESSORS = new BoundAttributes<>(
            adds("nonce", fromLong(MinimalAttestationRecord::nonce)),
            adds("index", fromInt(MinimalAttestationRecord::index)),
            adds("priorSH_48", fromLong(MinimalAttestationRecord::priorSH4_8)),
            adds("utcEpochSeconds", fromDouble(MinimalAttestationRecord::utcEpochSeconds)),
            adds("position", fromConverted(MinimalAttestationRecord::position, Geoposition::canonicalBytes)),
            adds("orientation", fromConverted(MinimalAttestationRecord::orientation, Orientation::canonicalBytes)),
            adds("currentSH4_8", fromLong(MinimalAttestationRecord::currentSH4_8)),
            adds("signature", fromConverted(MinimalAttestationRecord::signature, MarSignature::canonicalBytes)),
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
