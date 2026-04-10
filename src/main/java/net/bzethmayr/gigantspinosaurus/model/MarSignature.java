package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.util.Arrays;

public record MarSignature(
        byte[] ed25519Pub,
        byte[] ed25519,
        short version
) implements ExposesSignature {

    @Override
    public boolean equals(final Object other) {
        if (other instanceof MarSignature brother) {
            return Arrays.equals(ed25519, brother.ed25519)
                    && Arrays.equals(ed25519Pub, brother.ed25519Pub)
                    && version == brother.version;
        }
        return false;
    }

    public MarSignature(final byte[] ed25519Pub, final byte[] ed25519) {
        this(ed25519Pub, ed25519, SIGNATURE_VERSION);
    }
}
