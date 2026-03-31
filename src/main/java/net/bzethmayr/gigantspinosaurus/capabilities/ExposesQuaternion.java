package net.bzethmayr.gigantspinosaurus.capabilities;

public interface ExposesQuaternion<R extends ExposesQuaternion<R>> {
    double QW();
    double QX();
    double QY();
    double QZ();

    default double[] asQ4() {
        return new double[]{QW(), QX(), QY(), QZ()};
    }

    R withQ4(double[] q4);
}
