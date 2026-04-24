package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia.MEDIA_VERSION;

public class MarVerification {
    private final BindsConstructors ctors;
    private final BindsEnvironment env;

    public MarVerification(
            final BindsConstructors ctors,
            final BindsEnvironment env
    ) {
        this.ctors = ctors;
        this.env = env;
    }

    public final boolean verifyMar(final ExposesMar someFrame, final ByteBuffer media) {
        if (someFrame.index() < 0) {
            return verifyIntent(someFrame);
        }
        return verifyMedia(someFrame, media);
    }

    private MutableMar coreFieldsCopy(final ExposesMar someFrame) {
        final MutableMar hashingFrame = new MutableMar();
        hashingFrame.nonce(someFrame.nonce());
        hashingFrame.index(someFrame.index());
        hashingFrame.priorSipH4_8(someFrame.priorSipH4_8());
        hashingFrame.utcEpochSeconds(someFrame.utcEpochSeconds());
        hashingFrame.position(someFrame.position());
        hashingFrame.orientation(someFrame.orientation());
        return hashingFrame;
    }

    private boolean verifyHashAndSignature(final ExposesMar putative, final MutableMar hashingFrame) {
        final byte[] hashingBytes = hashingFrame.canonicalBytes();
        final long hash = env.marHasher().applyAsLong(hashingFrame.sipHashKey(), hashingBytes);
        if (hash != putative.currentSipH4_8()) {
            return false;
        }
        hashingFrame.currentSipH4_8(hash);
        final byte[] signingBytes = hashingFrame.canonicalBytes();
        return env.signatory().test(putative.signature(), signingBytes);
    }

    public final boolean verifyIntent(final ExposesMar intentFrame) {
        MutableMar hashingFrame = coreFieldsCopy(intentFrame);
        hashingFrame.media(intentFrame.media());
        return verifyHashAndSignature(intentFrame, hashingFrame);
    }

    public final boolean verifyMedia(final ExposesMar mediaFrame, final ByteBuffer media) {
        final MutableMar hashingFrame = coreFieldsCopy(mediaFrame);
        final ExposesMedia frameMedia = mediaFrame.media();
        hashingFrame.media(ctors.mediaCtor().createMedia(
                frameMedia.r0(),
                frameMedia.r1(),
                frameMedia.r2(),
                frameMedia.r3(),
                env.mediaHasher().apply(media),
                MEDIA_VERSION
        ));
        return verifyHashAndSignature(mediaFrame, hashingFrame);
    }
}
