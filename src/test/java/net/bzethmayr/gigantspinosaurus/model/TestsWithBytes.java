package net.bzethmayr.gigantspinosaurus.model;

public interface TestsWithBytes {

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

}
