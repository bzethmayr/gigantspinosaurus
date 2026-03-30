package net.bzethmayr.gigantspinosaurus.util;

import net.bzethmayr.gigantspinosaurus.capabilities.ExposesQuaternion;

import java.util.Arrays;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

public final class QuaternionHelper {

    public static void checkQ4(double[] q4) {
        if (q4 == null) {
            throw becauseIllegal("Quaternion array cannot be null");
        }
        if (q4.length != 4) {
            throw becauseIllegal("Quaternion array must have exactly 4 elements");
        }
    }

    /**
     * Normalizes a quaternion array (q4) in-place.
     * 
     * @param q4 the quaternion array [w, x, y, z] to normalize
     * @throws IllegalArgumentException if the input array is null or not of length 4
     */
    public static void normalize(double[] q4) {
        checkQ4(q4);
        normalizeUnchecked(q4);
    }

    private static void normalizeUnchecked(double[] q4) {
        double magnitude = Math.sqrt(q4[0] * q4[0] + q4[1] * q4[1] + q4[2] * q4[2] + q4[3] * q4[3]);

        if (magnitude == 0) {
            throw becauseIllegal("Cannot normalize zero quaternion");
        }

        q4[0] /= magnitude;
        q4[1] /= magnitude;
        q4[2] /= magnitude;
        q4[3] /= magnitude;
    }

    /**
     * Returns a normalized copy of the input quaternion array.
     * 
     * @param q4 the input quaternion array [w, x, y, z]
     * @return a new normalized quaternion array
     * @throws IllegalArgumentException if the input array is null or not of length 4
     */
    public static double[] normalized(double[] q4) {
        checkQ4(q4);
        final double[] new4 = Arrays.copyOf(q4, 4);
        normalizeUnchecked(q4);
        return new4;
    }

    public static double[] normalized(final ExposesQuaternion q4) {
        return normalized(new double[]{q4.QW(), q4.QX(), q4.QY(), q4.QZ()});
    }
}