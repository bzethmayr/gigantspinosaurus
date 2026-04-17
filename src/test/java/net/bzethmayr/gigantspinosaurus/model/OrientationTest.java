package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.RepeatedTest;

import static java.nio.ByteBuffer.wrap;
import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.decoders;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrientationTest implements TestsModel, TestsWithBytes {

    private Orientation underTest;

    void setUpTrivial() {
        underTest = randomOrientation();
    }

    @RepeatedTest(FEW)
    void decode_roundTrips() {
        setUpTrivial();

        final byte[] bytes = underTest.canonicalBytes();
        dump(bytes);
        final Orientation parsed = MarDecoder.decodeOrientation(wrap(bytes), decoders());

        assertEquals(underTest, parsed);
    }
}