package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;

import java.util.concurrent.atomic.AtomicReference;

import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.SIGNATURE_VERSION;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public class MarCreation {
    private final BindsConstructors ctors;
    private final BindsEnvironment env;

    @FunctionalInterface
    public interface MediaFrameReceiver {
        ExposesMar mediaFrame(final byte[] frameData, final int index);
    }

    public MarCreation(
            final BindsConstructors ctors,
            final BindsEnvironment env
    ) {
        this.ctors = ctors;
        this.env = env;
    }

    private static final class MutableMar implements ExposesMar {
        public long nonce;
        public int index;
        public long priorSH_48;
        public double utcEpochSeconds;
        public ExposesPosition position;
        public ExposesOrientation<?> orientation;
        public long currentSH_48;
        public ExposesSignature signature;

        @Override
        public long nonce() {
            return nonce;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public long priorSipHash4_8() {
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
        public long currentSipHash4_8() {
            return currentSH_48;
        }

        @Override
        public ExposesSignature signature() {
            return signature;
        }
    }

    public ExposesMar frameZero() {
        final GeneratesNonce nonceSource = env.nonceSource();
        final ExposesUtcDoubleSeconds timeSource = env.timeSource();
        final ExposesPosition positionSource = env.positionSource();
        final ExposesOrientation<?> orientationSource = env.orientationSource();
        final Signatory signatory = env.signatory();
        final MutableMar bufferZero = new MutableMar();
        bufferZero.nonce = nonceSource.getAsLong();
        bufferZero.index = -1;
        bufferZero.priorSH_48 = nonceSource.getAsLong();
        bufferZero.utcEpochSeconds = timeSource.utcDoubleSeconds();
        bufferZero.position = ctors.positionCtor().copyPosition(positionSource);
        bufferZero.orientation = ctors.orientationCtor().copyOrientation(orientationSource);
        final byte[] bufferZeroBytes = bufferZero.canonicalBytes();
        bufferZero.currentSH_48 = env.hasher().applyAsLong(bufferZero.sipHashKey(), bufferZeroBytes);
        final byte[] toSign = bufferZero.canonicalBytes();
        final byte[] signature = env.signatory().apply(toSign);
        bufferZero.signature = ctors.signatureCtor().createSignature(
                signatory.get(), signature, SIGNATURE_VERSION);
        return ctors.marCtor().copyMar(bufferZero);
    }

    public MediaFrameReceiver intentToRecord() {
        final AtomicReference<ExposesMar> priorFrame = new AtomicReference<>(frameZero());
        final Signatory signatory = env.signatory();

        return (media, index) -> {
            ExposesMar prior = priorFrame.get();
            final MutableMar nextBuffer = new MutableMar();
            nextBuffer.nonce = prior.nonce();
            nextBuffer.index = index;
            nextBuffer.priorSH_48 = prior.currentSipHash4_8();
            nextBuffer.utcEpochSeconds = env.timeSource().utcDoubleSeconds();
            nextBuffer.position = ctors.positionCtor().copyPosition(env.positionSource());
            nextBuffer.orientation = ctors.orientationCtor().copyOrientation(env.orientationSource());
            final byte[] hashingBytes = nextBuffer.canonicalBytes();
            nextBuffer.currentSH_48 = env.hasher().applyAsLong(prior.sipHashKey(), hashingBytes);
            final byte[] toSign = nextBuffer.canonicalBytes();
            final byte[] signature = env.signatory().apply(toSign);
            nextBuffer.signature = ctors.signatureCtor().createSignature(
                    signatory.get(), signature, SIGNATURE_VERSION
            );
            final ExposesMar next = ctors.marCtor().copyMar(nextBuffer);
            if (priorFrame.compareAndSet(prior, next)) {
                return next;
            }
            throw becauseImpossible("Series interrupted.");
        };
    }
}
