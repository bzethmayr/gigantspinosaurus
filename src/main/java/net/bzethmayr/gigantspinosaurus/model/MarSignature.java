package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromBytes;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public record MarSignature(
        byte[] ed25519Pub,
        byte[] ed25519,
        short version
) implements HasRequiredAttributes {

    public MarSignature(final byte[] ed25519Pub, final byte[] ed25519) {
        this(ed25519Pub, ed25519, (short) 0);
    }

    private static final BoundAttributes<MarSignature> ACCESSORS = new BoundAttributes<>(
            adds("ed25519Pub", fromBytes(MarSignature::ed25519Pub)),
            adds("ed25519", fromBytes(MarSignature::ed25519)),
            Versioned.addsVersion()
    );
    private static final SequencedSet<String> REQUIRED = ACCESSORS.fieldNamesExcept(VERSION_FIELD);


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
