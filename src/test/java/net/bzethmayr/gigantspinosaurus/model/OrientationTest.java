package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static java.nio.ByteBuffer.wrap;
import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.decoders;
import static org.junit.jupiter.api.Assertions.*;

class OrientationTest implements TestsModel, TestsWithBytes {

    private Orientation underTest;

    void setUpTrivial() {
        underTest = randomOrientation();
    }

    @RepeatedTest(1024)
    void decode_roundTrips() {
        setUpTrivial();

        final byte[] bytes = underTest.canonicalBytes();
        dump(bytes);
        final Orientation parsed = MarDecoder.decodeOrientation(wrap(bytes), decoders());

        assertEquals(underTest, parsed);
    }

    @Test
    void getRequiredAttributes_always_returnsAllAttributes() {

    }

    @Test
    void getCanonicalAttributes() {
    }

    @Test
    void getAttributeValue() {
    }

    @Test
    void QW() {
    }

    @Test
    void QX() {
    }

    @Test
    void QY() {
    }

    @Test
    void QZ() {
    }

    @Test
    void frame() {
    }
}