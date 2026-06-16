package net.bzethmayr.gigantspinosaurus.usage.defaults.desktop;

import net.bzethmayr.gigantspinosaurus.model.Framing;
import net.bzethmayr.gigantspinosaurus.model.framing.*;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import org.junit.jupiter.api.Test;

import static net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming.FRAMING_VERSION;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.FRONT;
import static net.bzethmayr.gigantspinosaurus.model.framing.Face.U_FACE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.RIGHT;
import static net.bzethmayr.gigantspinosaurus.model.framing.Handedness.LEFT;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.U_NORTH;
import static net.bzethmayr.gigantspinosaurus.model.framing.North.TRUE;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.UP;
import static net.bzethmayr.gigantspinosaurus.model.framing.Vertical.DOWN;
import static org.junit.jupiter.api.Assertions.*;

class DesktopOrientationTest {

    @Test
    void defaultConstructor_returnsOneZeroOneZero() {
        final var underTest = new DesktopOrientation();
        assertEquals(1.0, underTest.QW());
        assertEquals(0.0, underTest.QX());
        assertEquals(1.0, underTest.QY());
        assertEquals(0.0, underTest.QZ());
    }

    @Test
    void defaultConstructor_usesDefaultFraming() {
        final var underTest = new DesktopOrientation();
        final ExposesFraming framing = underTest.framing();
        assertEquals(RIGHT, framing.x());
        assertEquals(UP, framing.y());
        assertEquals(FRONT, framing.z());
        assertEquals(RIGHT, framing.handed());
        assertEquals(U_NORTH, framing.north());
    }

    @Test
    void parameterizedConstructor_preservesValues() {
        final var underTest = new DesktopOrientation(0.5, 0.5, 0.5, 0.5);
        assertEquals(0.5, underTest.QW());
        assertEquals(0.5, underTest.QX());
        assertEquals(0.5, underTest.QY());
        assertEquals(0.5, underTest.QZ());
    }

    @Test
    void fullConstructor_preservesAllValues() {
        final ExposesFraming customFraming = new Framing(
                LEFT, DOWN, U_FACE, LEFT, TRUE);
        final var underTest = new DesktopOrientation(2.0, 3.0, 4.0, 5.0, customFraming);
        assertEquals(2.0, underTest.QW());
        assertEquals(3.0, underTest.QX());
        assertEquals(4.0, underTest.QY());
        assertEquals(5.0, underTest.QZ());
        assertSame(customFraming, underTest.framing());
    }

    @Test
    void withQ4_returnsNewInstanceWithUpdatedQuaternion() {
        final var original = new DesktopOrientation(1, 0, 1, 0);
        final var updated = original.withQ4(new double[]{9, 8, 7, 6});
        assertEquals(9, updated.QW());
        assertEquals(8, updated.QX());
        assertEquals(7, updated.QY());
        assertEquals(6, updated.QZ());
        assertNotSame(original, updated);
    }

    @Test
    void withQ4_preservesOriginalFraming() {
        final var original = new DesktopOrientation(1, 0, 1, 0);
        final var updated = original.withQ4(new double[]{9, 8, 7, 6});
        assertSame(original.framing(), updated.framing());
    }

    @Test
    void withFraming_returnsNewInstanceWithUpdatedFraming() {
        final var original = new DesktopOrientation(1, 0, 1, 0);
        final ExposesFraming newFraming = new Framing(
                RIGHT, UP, U_FACE, LEFT, TRUE);
        final var updated = original.withFraming(newFraming);
        assertSame(newFraming, updated.framing());
        assertEquals(1, updated.QW());
        assertNotSame(original, updated);
    }

    @Test
    void version_returnsOrientationVersion() {
        final var underTest = new DesktopOrientation();
        assertEquals(ExposesOrientation.ORIENTATION_VERSION, underTest.version());
    }
}
