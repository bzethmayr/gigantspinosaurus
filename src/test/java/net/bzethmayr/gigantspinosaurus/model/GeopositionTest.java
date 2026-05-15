package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.usage.MarDecoding;
import org.junit.jupiter.api.RepeatedTest;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.MarDecoding.decoders;
import static org.junit.jupiter.api.Assertions.*;

class GeopositionTest implements TestsModel {

    @RepeatedTest(1024)
    void decode_canDecodeCanonicalBytes() {
        Geoposition underTest = randomGeoposition();

        final byte[] bytes = underTest.canonicalBytes();
        final Geoposition parsed = MarDecoding.decodePosition(ByteBuffer.wrap(bytes), decoders());

        assertEquals(underTest, parsed);
    }
}