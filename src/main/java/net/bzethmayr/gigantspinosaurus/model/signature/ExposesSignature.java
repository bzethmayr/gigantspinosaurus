package net.bzethmayr.gigantspinosaurus.model.signature;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasRequiredAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromBytes;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface ExposesSignature extends HasRequiredAttributes {
    short SIGNATURE_VERSION = 0;
    String PUB_KEY_FIELD = "ed25519Pub";
    int PUB_KEY_LENGTH = 32;
    String SIGNATURE_FIELD = "ed25519";
    int SIGNATURE_LENGTH = 64;


    byte[] ed25519Pub();
    byte[] ed25519();
    BoundAttributes<ExposesSignature> ACCESSORS = new BoundAttributes<>(
            adds(PUB_KEY_FIELD, fromBytes(ExposesSignature::ed25519Pub)),
            adds(SIGNATURE_FIELD, fromBytes(ExposesSignature::ed25519)),
            Versioned.addsVersion()
    );
    SequencedSet<String> REQUIRED = ACCESSORS.fieldNamesExcept(VERSION_FIELD);

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
