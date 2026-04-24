package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.Media;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia.MEDIA_HASH_BYTES;
import static net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia.MEDIA_VERSION;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionStep.noStep;
import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.SIGNATURE_VERSION;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public class MarCreation {
    private final BindsConstructors ctors;
    private final BindsEnvironment env;

    @FunctionalInterface
    public interface MediaFrameReceiver {
        ExposesMar mediaFrame(final ByteBuffer frameData, final int index);
    }

    public MarCreation(
            final BindsConstructors ctors,
            final BindsEnvironment env
    ) {
        this.ctors = ctors;
        this.env = env;
    }

    private static ReductionStep stepOrNoStep(final int index, final ReductionStep... steps) {
        return index < steps.length && index > -1
                ? steps[index]
                : noStep();
    }

    public ExposesMar intentFrame(final ReductionStep... reductionSteps) {
        final GeneratesNonce nonceSource = env.nonceSource();
        final ExposesUtcDoubleSeconds timeSource = env.timeSource();
        final ExposesPosition positionSource = env.positionSource();
        final ExposesOrientation<?> orientationSource = env.orientationSource();
        final Signatory signatory = env.signatory();
        final MutableMar bufferZero = new MutableMar();
        bufferZero.nonce(nonceSource.getAsLong());
        bufferZero.index(-1);
        bufferZero.priorSipH4_8(nonceSource.getAsLong());
        bufferZero.utcEpochSeconds(timeSource.utcDoubleSeconds());
        bufferZero.position(ctors.positionCtor().copyPosition(positionSource));
        bufferZero.orientation(ctors.orientationCtor().copyOrientation(orientationSource));
        bufferZero.media(ctors.mediaCtor().createMedia(
                stepOrNoStep(0, reductionSteps),
                stepOrNoStep(1, reductionSteps),
                stepOrNoStep(2, reductionSteps),
                stepOrNoStep(3, reductionSteps),
                new byte[MEDIA_HASH_BYTES], MEDIA_VERSION));
        final byte[] bufferZeroBytes = bufferZero.canonicalBytes();
        bufferZero.currentSipH4_8(env.marHasher().applyAsLong(bufferZero.sipHashKey(), bufferZeroBytes));
        final byte[] toSign = bufferZero.canonicalBytes();
        final byte[] signature = env.signatory().apply(toSign);
        bufferZero.signature(ctors.signatureCtor().createSignature(
                signatory.get(), signature, SIGNATURE_VERSION));
        return ctors.marCtor().copyMar(bufferZero);
    }

    public MediaFrameReceiver intentToRecord() {
        ExposesMar intentFrame = intentFrame();
        return intentToRecord(intentFrame);
    }

    public MediaFrameReceiver intentToRecord(final ExposesMar intentFrame) {
        final AtomicReference<ExposesMar> priorFrame = new AtomicReference<>(intentFrame);
        final AtomicInteger priorIndex = new AtomicInteger(intentFrame.index());
        final Signatory signatory = env.signatory();

        return (media, index) -> {
            if (index <= priorIndex.get()) {
                throw becauseIllegal("Index regression...");
            }
            ExposesMar prior = priorFrame.get();
            final MutableMar nextBuffer = new MutableMar();
            nextBuffer.nonce(prior.nonce());
            nextBuffer.index(index);
            nextBuffer.priorSipH4_8(prior.currentSipH4_8());
            nextBuffer.utcEpochSeconds(env.timeSource().utcDoubleSeconds());
            nextBuffer.position(ctors.positionCtor().copyPosition(env.positionSource()));
            nextBuffer.orientation(ctors.orientationCtor().copyOrientation(env.orientationSource()));
            final ExposesMedia priorMedia = prior.media();
            nextBuffer.media(ctors.mediaCtor().createMedia(
                    priorMedia.r0(),
                    priorMedia.r1(),
                    priorMedia.r2(),
                    priorMedia.r3(),
                    env.mediaHasher().apply(media),
                    MEDIA_VERSION
            ));
            final byte[] conditions = nextBuffer.canonicalBytes();
            nextBuffer.currentSipH4_8(env.marHasher().applyAsLong(nextBuffer.sipHashKey(), conditions));
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
