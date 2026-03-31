package net.bzethmayr.gigantspinosaurus;

import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;

public interface TestsWithEnums {

    default <E extends Enum<E>> E randomEnum(final Class<E> enumClass) {
        final E[] values = enumClass.getEnumConstants();
        return values[TEST_RANDOM.nextInt(values.length)];
    }
}
