package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.framing.Face;
import net.bzethmayr.gigantspinosaurus.model.framing.Handedness;
import net.bzethmayr.gigantspinosaurus.model.framing.North;
import net.bzethmayr.gigantspinosaurus.model.framing.Vertical;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.capabilities.Versioned.VERSION_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.decoders;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FramingTest implements TestsModel, TestsWithBytes {
    final Handedness x = randomHand();
    final Vertical y = randomVert();
    final Face z = randomFace();
    final Handedness handed = randomHand();
    final North north = randomNorth();
    final short version = randomVersion();

    private Framing underTest;

    @BeforeEach
    void setUpUnderTest() {
        underTest = new Framing(x, y, z, handed, north, version);
    }

    @Test
    void decode_roundTripsExact() {

        final byte[] bytes = underTest.canonicalBytes();
        dump(bytes);
        final Framing parsed = MarDecoder.decodeFraming(ByteBuffer.wrap(bytes), decoders());

        assertEquals(underTest, parsed);
    }

    @Test
    void getCanonicalAttributes_always_returnsAllAttributes() {

        assertThat(underTest.getCanonicalAttributes(), contains(VERSION_FIELD, "x", "y", "z", "handed", "north"));
    }

    @Test
    void getAttributeValue_givenAnyCanonical_returnsValue() {
        underTest.getCanonicalAttributes().forEach(s ->

                assertNotNull(underTest.getAttributeValue(s)));
    }

    @Test
    void x() {

        assertEquals(x, underTest.x());
    }

    @Test
    void y() {

        assertEquals(y, underTest.y());
    }

    @Test
    void z() {

        assertEquals(z, underTest.z());
    }

    @Test
    void handed() {

        assertEquals(handed, underTest.handed());
    }

    @Test
    void north() {

        assertEquals(north, underTest.north());
    }

    @Test
    void version() {

        assertEquals(version, underTest.version());
    }
}