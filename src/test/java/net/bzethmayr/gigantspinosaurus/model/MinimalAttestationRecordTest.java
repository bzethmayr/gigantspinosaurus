package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimalAttestationRecordTest implements TestsModel, TestsWithBytes {

    private MinimalAttestationRecord underTest;

    void setUpRandomUnderTest() {
        underTest = minimalRandomizedMar();
    }

    void setUpNormalUnderTest() {
        underTest = realisticRandomizedMar();
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
    void minimalCanonicalBytes_producesParsableShape() {
        setUpRandomUnderTest();

        final byte[] result = underTest.canonicalBytes();

        assertThat(result.length, greaterThan(2));
        final String garbled = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(result)).toString();
        System.out.println(garbled);
    }

    @Test
    void normalCanonicalBytes_producesParsableShape() {
        setUpNormalUnderTest();

        final byte[] result = underTest.canonicalBytes();

        assertThat(result.length, greaterThan(2));
        final String garbled = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(result)).toString();
        System.out.println(garbled);
    }

    @RepeatedTest(1024)
    void normalCanonicalBytes_roundTripsExact() {
        setUpNormalUnderTest();

        final byte[] serialForm = underTest.canonicalBytes();
        dump(serialForm);
        final MinimalAttestationRecord parsed = MarDecoder.decode(ByteBuffer.wrap(serialForm));

        assertEquals(underTest, parsed);
    }

    @RepeatedTest(1024)
    void randomBytes_throws() {
        final byte[] randomBytes = fakeMediaBytes(580);

        assertThrows(IllegalArgumentException.class, () ->
                MarDecoder.decode(ByteBuffer.wrap(randomBytes)));
    }
}