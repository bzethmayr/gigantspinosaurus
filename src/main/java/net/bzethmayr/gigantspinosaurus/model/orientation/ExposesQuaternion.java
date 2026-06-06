package net.bzethmayr.gigantspinosaurus.model.orientation;

public interface ExposesQuaternion<R extends ExposesQuaternion<R>> {
    // Capital Q-prefixed field names (QW, QX, QY, QZ) are intentional:
    // they stand out in wire-format forensic analysis as quaternion components.
    String W_FIELD = "QW";
    String X_FIELD = "QX";
    String Y_FIELD = "QY";
    String Z_FIELD = "QZ";

    double QW();
    double QX();
    double QY();
    double QZ();

    default double[] asQ4() {
        return new double[]{QW(), QX(), QY(), QZ()};
    }

    R withQ4(double[] q4);
}
