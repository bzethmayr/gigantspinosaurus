package net.bzethmayr.gigantspinosaurus.model.signature;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;

import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromBytes;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public interface ExposesSignature extends HasCanonicalAttributes {
    /**
     * 1... - wrong lengths
     * 2 - 32 bytes key, 64 bytes signature (also wrong)
     * 3 - 44 bytes key, 64 bytes signature ?
     */
    short SIGNATURE_VERSION = 3;
    String PUB_KEY_FIELD = "ed25519Pub";
    int PUB_KEY_LENGTH = 44;
    String SIGNATURE_FIELD = "ed25519";
    int SIGNATURE_LENGTH = 64;

    byte[] ed25519Pub();
    byte[] ed25519();
    BoundAttributes<ExposesSignature> ACCESSORS = new BoundAttributes<>(
            ExposesSignature.class,
            Versioned.addsVersion(),
            adds(PUB_KEY_FIELD, fromBytes(ExposesSignature::ed25519Pub)),
            adds(SIGNATURE_FIELD, fromBytes(ExposesSignature::ed25519))
    );

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }

    static IllegalStateException becauseEdHasGone() {
        return becauseImpossible("Ed25519 has gone?");
    }
}
