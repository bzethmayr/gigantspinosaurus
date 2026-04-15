package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMarFrame;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMedia;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;
import net.bzethmayr.gigantspinosaurus.usage.desktop.DesktopOrientation;
import net.bzethmayr.gigantspinosaurus.usage.desktop.DesktopPosition;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public record BindsEnvironment(GeneratesNonce nonceSource,
                               HashesMarFrame marHasher,
                               HashesMedia mediaHasher,
                               ExposesUtcDoubleSeconds timeSource,
                               ExposesPosition positionSource,
                               ExposesOrientation<?> orientationSource,
                               ExposesFraming framingSource,
                               Signatory signatory) {

    public static BindsEnvironment desktopEnvironment() {
        final SecureRandom random = new SecureRandom();
        final SipMarHasher hashesMar = new SipMarHasher();
        final Xx64MediaHasher hashesMedia = new Xx64MediaHasher();
        final Clock utcClock = Clock.systemUTC();
        final DesktopOrientation fixedOrientation = new DesktopOrientation();
        final KeyPairGenerator ephemeral;
        try {
            ephemeral = KeyPairGenerator.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseImpossible("Ed25519 has gone?");
        }
        final KeyPair ephemeralPair = ephemeral.generateKeyPair();
        return new BindsEnvironment(
                random::nextLong,
                hashesMar,
                hashesMedia,
                () -> utcClock.millis() / 1000d,
                new DesktopPosition(),
                fixedOrientation,
                fixedOrientation.framing(),
                new SignsForJava15(ephemeralPair)
        );
    }
}
