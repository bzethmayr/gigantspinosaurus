package net.bzethmayr.gigantspinosaurus.util;

import net.bzethmayr.gigantspinosaurus.TestsWithQuaternions;
import net.bzethmayr.gigantspinosaurus.model.Frame;
import net.bzethmayr.gigantspinosaurus.model.Orientation;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static net.bzethmayr.gigantspinosaurus.util.QuaternionHelper.*;
import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.*;

class QuaternionHelperTest implements TestsWithQuaternions {

    @Test
    void checkQ4_givenNull_throws() {

        assertThrows(IllegalArgumentException.class, () -> checkQ4(null));
    }

    @Test
    void checkQ4_givenThree_throws() {

        assertThrows(IllegalArgumentException.class, () -> checkQ4(new double[3]));
    }

    @Test
    void checkQ4_givenFive_throws() {

        assertThrows(IllegalArgumentException.class, () -> checkQ4(new double[5]));
    }

    @Test
    void checkQ4_givenFour_doesNothing() {

        assertDoesNotThrow(() -> checkQ4(new double[4]));
    }

    @Test
    void normalize_givenFourEmpty_throws() {

        assertThrows(IllegalArgumentException.class, () -> normalize(new double[4]));
    }

    @ParameterizedTest
    @MethodSource("unitQ4s")
    void normalize_givenUnitQ4_doesNothing(final double[] q4) {
        final double w = q4[0];
        final double x = q4[1];
        final double y = q4[2];
        final double z = q4[3];

        normalize(q4);

        assertEquals(w, q4[0]);
        assertEquals(x, q4[1]);
        assertEquals(y, q4[2]);
        assertEquals(z, q4[3]);
    }

    @RepeatedTest(1024)
    void normalize_twice_doesNotChangeFirstResultMuch() {
        final double[] q4 = new double[4];
        for (int i = 0; i < 4; i++) {
            q4[i] = TEST_RANDOM.nextDouble();
        }

        normalize(q4);
        final double w = q4[0];
        final double x = q4[1];
        final double y = q4[2];
        final double z = q4[3];
        normalize(q4);
        assertThat(q4[0], closeTo(w, Q_ERROR_LIMIT));
        assertThat(q4[1], closeTo(x, Q_ERROR_LIMIT));
        assertThat(q4[2], closeTo(y, Q_ERROR_LIMIT));
        assertThat(q4[3], closeTo(z, Q_ERROR_LIMIT));
    }

    @ParameterizedTest
    @MethodSource("unitQ4s")
    void normalized_givenFourOnes_producesSameFourOnes(final double[] q4) {

        final double[] result = normalized(q4);

        assertNotSame(q4, result);
        assertArrayEquals(q4, result);
    }

    @ParameterizedTest
    @MethodSource("unitQ4s")
    void normalized_givenExposesQuaternion_returnsNormalizedInstance(final double[] q4) {
        final Orientation orientation = new Orientation(q4[0], q4[1], q4[2], q4[3], new Frame());

        final Orientation result = normalized(orientation);

        assertNotSame(orientation, result);
        assertEquals(q4[0], result.QW());
        assertEquals(q4[1], result.QX());
        assertEquals(q4[2], result.QY());
        assertEquals(q4[3], result.QZ());
    }
}