package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

class MinimalAttestationRecordTest {

    private MinimalAttestationRecord underTest;

    void setUpRandomUnderTest() {
        underTest = new MinimalAttestationRecord(
                TEST_RANDOM.nextLong(),
                TEST_RANDOM.nextInt(333333),
                TEST_RANDOM.nextLong(),
                TEST_RANDOM.nextDouble(),
                new Geoposition(0.0d, 0.0d, 0.0d),
                new Orientation(1.0d, 1.0, 0.0d, 0.0d, new Frame()),
                TEST_RANDOM.nextLong(),
                new MarSignature(new byte[]{}, new byte[]{}),
                (short) 0
        );
    }

    void setUpNormalUnderTest() {
        final byte[] fakePub = new byte[64];
        TEST_RANDOM.nextBytes(fakePub);
        final byte[] fakeSign = new byte[128];
        TEST_RANDOM.nextBytes(fakeSign);
        underTest = new MinimalAttestationRecord(
                TEST_RANDOM.nextLong(),
                0,
                0L,
                Instant.now().toEpochMilli() / 1000d,
                new Geoposition(
                        TEST_RANDOM.nextDouble(-180d, 180d),
                        TEST_RANDOM.nextDouble(-90d, 90d),
                        TEST_RANDOM.nextDouble(-7000, 7000)),
                new Orientation(
                        TEST_RANDOM.nextDouble(-1d, 1d),
                        TEST_RANDOM.nextDouble(-1d, 1d),
                        TEST_RANDOM.nextDouble(-1d, 1d),
                        TEST_RANDOM.nextDouble(-1d, 1d),
                        new Frame()),
                TEST_RANDOM.nextLong(),
                new MarSignature(fakePub, fakeSign),
                (short) 0
        );
    }

    @Test
    void getRequiredAttributes() {
    }

    @Test
    void getCanonicalAttributes() {
    }

    @Test
    void getAttributeValue() {
    }

    @Test
    void canonicalBytes_producesParsableShape() {
        setUpRandomUnderTest();

        final byte[] result = underTest.canonicalBytes();

        assertThat(result.length, greaterThan(2));
        final String garbled = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(result)).toString();
        System.out.println(garbled);
    }

    @Test
    void normalBytes_producesParsableShape() {
        setUpNormalUnderTest();

        final byte[] result = underTest.canonicalBytes();

        assertThat(result.length, greaterThan(2));
        final String garbled = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(result)).toString();
        System.out.println(garbled);
    }

}