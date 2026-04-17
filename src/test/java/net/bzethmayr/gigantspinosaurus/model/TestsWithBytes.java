package net.bzethmayr.gigantspinosaurus.model;

import java.nio.ByteBuffer;

import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;

public interface TestsWithBytes {
    int FEW  = 1 << 8;   // L1-ish bytes
    int SOME = 1 << 12;  // page-ish bytes
    int MANY = 1 << 16;  // now we're talking bytes
    int LOTS = 1 << 20;  // one _million_ (...

    default void printOrSomething(final byte b) {
        if (b > 32) {
            System.out.printf("%s   ", Character.toString(b));
        } else {
            System.out.printf("%s ", b);
        }
    }

    default void dump(final byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            printOrSomething(bytes[i]);
            if (i % 16 == 15) {
                System.out.println();
            }
        }
    }

    default ByteBuffer fakeMediaBytes(final int length) {
        final byte[] fake = new byte[length];
        TEST_RANDOM.nextBytes(fake);
        return ByteBuffer.wrap(fake);
    }

}
