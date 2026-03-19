package net.bzethmayr.gigantspinosaurus.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FrameTest implements  TestsWithEnums {
    final Handedness x = randomEnum(Handedness.class);
    final Vertical y = randomEnum(Vertical.class);
    final Face z = randomEnum(Face.class);
    final Handedness handed = randomEnum(Handedness.class);
    final North north = randomEnum(North.class);
    final int version = TEST_RANDOM.nextInt();

    private Frame underTest;

    @BeforeEach
    void setUpUnderTest() {
        underTest = new Frame(x, y, z, handed, north, version);
    }

    @Test
    void getRequiredAttributes_always_returnsAllAttributes() {

        assertThat(underTest.getRequiredAttributes(), contains("x", "y", "z", "handed", "north", "version"));
    }

    @Test
    void getCanonicalAttributes_always_returnsAllAttributes() {

        assertThat(underTest.getCanonicalAttributes(), contains("x", "y", "z", "handed", "north", "version"));
    }

    @Test
    void getAttributeValue_givenAnyRequired_returnsValue() {
        underTest.getRequiredAttributes().forEach(s ->

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