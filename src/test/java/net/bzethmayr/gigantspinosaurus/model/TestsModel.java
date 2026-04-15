package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.TestsWithEnums;
import net.bzethmayr.gigantspinosaurus.model.framing.Face;
import net.bzethmayr.gigantspinosaurus.model.framing.Handedness;
import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.framing.Vertical;

import java.time.Instant;

import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.MEDIA_HASH_BYTES;
import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.*;
import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;

public interface TestsModel extends TestsWithEnums {
    default MinimalAttestationRecord minimalRandomizedMar() {
        return new MinimalAttestationRecord(
                TEST_RANDOM.nextLong(),
                TEST_RANDOM.nextInt(333333),
                TEST_RANDOM.nextLong(),
                TEST_RANDOM.nextDouble(),
                zeroGeoposition(),
                oneOrientation(),
                new byte[MEDIA_HASH_BYTES],
                TEST_RANDOM.nextLong(),
                nullSignature(),
                (short) 0
        );
    }

    default MarSignature nullSignature() {
        return new MarSignature(new byte[PUB_KEY_LENGTH], new byte[SIGNATURE_LENGTH]);
    }

    default Framing noFrame() {
        return new Framing();
    }

    default Orientation oneOrientation() {
        return new Orientation(1.0d, 1.0, 0.0d, 0.0d, noFrame());
    }

    default Geoposition zeroGeoposition() {
        return new Geoposition(0.0d, 0.0d, 0.0d);
    }

    default Geoposition randomGeoposition() {
        return new Geoposition(
                TEST_RANDOM.nextDouble(-180d, 180d),
                TEST_RANDOM.nextDouble(-90d, 90d),
                TEST_RANDOM.nextDouble(-7000, 7000));
    }

    default Handedness randomHand() {
        return randomEnum(Handedness.class);
    }

    default Vertical randomVert() {
        return randomEnum(Vertical.class);
    }

    default Face randomFace() {
        return randomEnum(Face.class);
    }

    default North randomNorth() {
        return randomEnum(North.class);
    }

    default short randomVersion() {
        return (short) TEST_RANDOM.nextInt();
    }

    default Framing randomFraming() {
        return new Framing(randomHand(), randomVert(), randomFace(), randomHand(), randomNorth(), randomVersion());
    }

    default Orientation randomOrientation() {
        return new Orientation(
                TEST_RANDOM.nextDouble(-1d, 1d),
                TEST_RANDOM.nextDouble(-1d, 1d),
                TEST_RANDOM.nextDouble(-1d, 1d),
                TEST_RANDOM.nextDouble(-1d, 1d),
                new Framing());
    }

    default MarSignature randomSignature() {
        final byte[] fakePub = new byte[PUB_KEY_LENGTH];
        TEST_RANDOM.nextBytes(fakePub);
        final byte[] fakeSign = new byte[SIGNATURE_LENGTH];
        TEST_RANDOM.nextBytes(fakeSign);
        return new MarSignature(fakePub, fakeSign);
    }

    default MinimalAttestationRecord realisticRandomizedMar() {
        final byte[] fakeMedia = new byte[MEDIA_HASH_BYTES];
        TEST_RANDOM.nextBytes(fakeMedia);
        return new MinimalAttestationRecord(
                TEST_RANDOM.nextLong(),
                0,
                0L,
                Instant.now().toEpochMilli() / 1000d,
                randomGeoposition(),
                randomOrientation(),
                fakeMedia,
                TEST_RANDOM.nextLong(),
                randomSignature(),
                SIGNATURE_VERSION
        );
    }
}
