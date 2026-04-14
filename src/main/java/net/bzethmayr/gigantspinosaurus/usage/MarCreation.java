package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.SIGNATURE_VERSION;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
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

    public ExposesMar intentFrame() {
        final GeneratesNonce nonceSource = env.nonceSource();
        final ExposesUtcDoubleSeconds timeSource = env.timeSource();
        final ExposesPosition positionSource = env.positionSource();
        final ExposesOrientation<?> orientationSource = env.orientationSource();
        final Signatory signatory = env.signatory();
        final MutableMar bufferZero = new MutableMar();
        bufferZero.nonce(nonceSource.getAsLong());
        bufferZero.index(-1);
        bufferZero.prev_Mxx64_FsipH4_8(nonceSource.getAsLong());
        bufferZero.utcEpochSeconds(timeSource.utcDoubleSeconds());
        bufferZero.position(ctors.positionCtor().copyPosition(positionSource));
        bufferZero.orientation(ctors.orientationCtor().copyOrientation(orientationSource));
        final byte[] bufferZeroBytes = bufferZero.canonicalBytes();
        bufferZero.curr_Mxx64_FsipH4_8(env.marHasher().applyAsLong(bufferZero.sipHashKey(), bufferZeroBytes));
        final byte[] toSign = bufferZero.canonicalBytes();
        final byte[] signature = env.signatory().apply(toSign);
        bufferZero.signature(ctors.signatureCtor().createSignature(
                signatory.get(), signature, SIGNATURE_VERSION));
        return ctors.marCtor().copyMar(bufferZero);
    }

    public MediaFrameReceiver intentToRecord() {
        ExposesMar frameZero = intentFrame();
        final AtomicReference<ExposesMar> priorFrame = new AtomicReference<>(frameZero);
        final AtomicInteger priorIndex = new AtomicInteger(frameZero.index());
        final Signatory signatory = env.signatory();

        return (media, index) -> {
            if (index <= priorIndex.get()) {
                throw becauseIllegal("Index regression...");
            }
            ExposesMar prior = priorFrame.get();
            final MutableMar nextBuffer = new MutableMar();
            nextBuffer.nonce(prior.nonce());
            nextBuffer.index(index);
            nextBuffer.prev_Mxx64_FsipH4_8(prior.curr_Mxx64_FsipH4_8());
            nextBuffer.utcEpochSeconds(env.timeSource().utcDoubleSeconds());
            nextBuffer.position(ctors.positionCtor().copyPosition(env.positionSource()));
            nextBuffer.orientation(ctors.orientationCtor().copyOrientation(env.orientationSource()));
            nextBuffer.curr_Mxx64_FsipH4_8(env.mediaHasher().applyAsLong(media));
            final byte[] conditions = nextBuffer.canonicalBytes();
            nextBuffer.curr_Mxx64_FsipH4_8(env.marHasher().applyAsLong(prior.sipHashKey(), conditions));
            final byte[] toSign = nextBuffer.canonicalBytes();
            final byte[] signature = env.signatory().apply(toSign);
            nextBuffer.signature(ctors.signatureCtor().createSignature(
                    signatory.get(), signature, SIGNATURE_VERSION
            ));
            final ExposesMar next = ctors.marCtor().copyMar(nextBuffer);
            if (priorFrame.compareAndSet(prior, next)) {
                priorIndex.set(index);
                return next;
            }
            throw becauseImpossible("Series interrupted.");
        };
    }
}
