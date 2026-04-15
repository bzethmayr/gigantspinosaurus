package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.util.Arrays;
import java.util.Objects;

public record MinimalAttestationRecord(
        long nonce,
        int index,
        long priorSipH4_8,
        double utcEpochSeconds,
        ExposesPosition position,
        ExposesOrientation<?> orientation,
        byte[] mediaBLK3,
        long currentSipH4_8,
        ExposesSignature signature,
        short version
) implements ExposesMar {
    @Override
    public boolean equals(final Object other) {
        if (other instanceof MinimalAttestationRecord brother) {
            return nonce == brother.nonce
                    && index == brother.index
                    && priorSipH4_8 == brother.priorSipH4_8
                    && utcEpochSeconds == brother.utcEpochSeconds
                    && Objects.equals(position, brother.position)
                    && Objects.equals(orientation, brother.orientation)
                    && Arrays.equals(mediaBLK3, brother.mediaBLK3)
                    && currentSipH4_8 == brother.currentSipH4_8
                    && Objects.equals(signature, brother.signature)
                    && version == brother.version;
        }
        return false;
    }

    public MinimalAttestationRecord(long nonce,
                                    int index,
                                    long priorSH4_8,
                                    double utcEpochSeconds,
                                    ExposesPosition position,
                                    ExposesOrientation<?> orientation,
                                    byte[] mediaBLK3,
                                    long currentSH4_8,
                                    ExposesSignature signature) {
        this(nonce, index, priorSH4_8, utcEpochSeconds, position, orientation,
                mediaBLK3, currentSH4_8, signature, MAR_VERSION);
    }
}
