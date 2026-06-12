package net.bzethmayr.gigantspinosaurus.usage.defaults;

import net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment;
import net.bzethmayr.gigantspinosaurus.usage.defaults.desktop.DesktopOrientation;
import net.bzethmayr.gigantspinosaurus.usage.defaults.desktop.DesktopPosition;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;

import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.SIGNATURE_ALGORITHM;
import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.becauseEdHasGone;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseFactory;

public final class DefaultEnvironments {
    private DefaultEnvironments() {
        throw becauseFactory();
    }

    public static BindsEnvironment partialEnvironment() {
        final SecureRandom random = new SecureRandom();
        final SipMarHasher hashesMar = new SipMarHasher();
        final Blake3MediaHasher hashesMedia = new Blake3MediaHasher();
        final Clock utcClock = Clock.systemUTC();
        return new BindsEnvironment(
                random::nextLong,
                hashesMar,
                hashesMedia,
                () -> utcClock.millis() / 1000d,
                null, null, null, null
        );
    }

    public static BindsEnvironment desktopEnvironment() {
        final KeyPairGenerator ephemeral;
        try {
            ephemeral = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        }
        final KeyPair ephemeralPair = ephemeral.generateKeyPair();
        return partialEnvironment()
                .withPosition(new DesktopPosition())
                .withOrientation(new DesktopOrientation())
                .withSignatory(new SignsForJava15(ephemeralPair));
    }
}
