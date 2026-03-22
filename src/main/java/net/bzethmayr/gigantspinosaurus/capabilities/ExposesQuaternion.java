package net.bzethmayr.gigantspinosaurus.capabilities;

public interface ExposesQuaternion {
    double QW();
    double QX();
    double QY();
    double QZ();
    default double[] q4() {
        return new double[]{QW(), QX(), QY(), QZ()};
    }
}
