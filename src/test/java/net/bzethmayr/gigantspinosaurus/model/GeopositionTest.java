package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.decoders;
import static org.junit.jupiter.api.Assertions.*;

class GeopositionTest implements TestsModel {

    @RepeatedTest(1024)
    void decode_canDecodeCanonicalBytes() {
        Geoposition underTest = randomGeoposition();

        final byte[] bytes = underTest.canonicalBytes();
        final Geoposition parsed = MarDecoder.decodePosition(ByteBuffer.wrap(bytes), decoders());

        assertEquals(underTest, parsed);
    }
}