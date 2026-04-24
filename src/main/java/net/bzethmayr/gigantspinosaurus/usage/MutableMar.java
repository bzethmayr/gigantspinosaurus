package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

final class MutableMar implements ExposesMar {
    private long nonce;
    private int index;
    private long priorSH_48;
    private double utcEpochSeconds;
    private ExposesPosition position;
    private ExposesOrientation<?> orientation;
    private ExposesMedia media;
    private long currentSH_48;
    private ExposesSignature signature;


    public MutableMar signature(ExposesSignature signature) {
        this.signature = signature;
        return this;
    }

    public MutableMar currentSipH4_8(long currentSH_48) {
        this.currentSH_48 = currentSH_48;
        return this;
    }

    public MutableMar media(final ExposesMedia media) {
        this.media = media;
        return this;
    }

    public MutableMar orientation(ExposesOrientation<?> orientation) {
        this.orientation = orientation;
        return this;
    }

    public MutableMar position(ExposesPosition position) {
        this.position = position;
        return this;
    }

    public MutableMar utcEpochSeconds(double utcEpochSeconds) {
        this.utcEpochSeconds = utcEpochSeconds;
        return this;
    }

    public MutableMar priorSipH4_8(long priorSH_48) {
        this.priorSH_48 = priorSH_48;
        return this;
    }

    public MutableMar index(int index) {
        this.index = index;
        return this;
    }

    public MutableMar nonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public long nonce() {
        return nonce;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public long priorSipH4_8() {
        return priorSH_48;
    }

    @Override
    public double utcEpochSeconds() {
        return utcEpochSeconds;
    }

    @Override
    public ExposesPosition position() {
        return position;
    }

    @Override
    public ExposesOrientation<?> orientation() {
        return orientation;
    }

    @Override
    public ExposesMedia media() {
        return media;
    }

    @Override
    public long currentSipH4_8() {
        return currentSH_48;
    }

    @Override
    public ExposesSignature signature() {
        return signature;
    }
}
