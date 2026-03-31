package net.bzethmayr.gigantspinosaurus.capabilities.orientation;

import java.util.stream.Stream;

public interface TestsWithQuaternions {
    double Q_ERROR_LIMIT = 0.000000000000001d;

    static Stream<double[]> unitQ4s() {
        return Stream.of(
                new double[]{1d,0d,0d,0d},
                new double[]{0d,1d,0d,0d},
                new double[]{0d,0d,1d,0d},
                new double[]{0d,0d,0d,1d},
                new double[]{0d,-1d,0d,0d},
                new double[]{0d,0d,-1d,0d},
                new double[]{0d,0d,0d,-1d}
             );
    }
}
