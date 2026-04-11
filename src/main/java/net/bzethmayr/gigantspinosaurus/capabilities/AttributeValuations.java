package net.bzethmayr.gigantspinosaurus.capabilities;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseNotInstantiable;

public final class AttributeValuations {
    private AttributeValuations() {
        throw becauseNotInstantiable();
    }

    private static final byte[] NO_BYTES = new byte[0];

    public static <T> Function<T, byte[]> fromBytes(final Function<T, byte[]> byteGetter) {
        return byteGetter;
    }

    public static <T> Function<T, byte[]> fromLong(final Function<T, Long> longGetter) {
        return t -> ByteBuffer.allocate(Long.BYTES)
                .putLong(longGetter.apply(t))
                .array();
    }

    public static <T> Function<T, byte[]> fromDouble(final Function<T, Double> doubleGetter) {
        return t -> ByteBuffer.allocate(Double.BYTES)
                .putDouble(doubleGetter.apply(t))
                .array();
    }

    public static <T, X> Function<T, byte[]> fromConverted(
            final Function<T, X> objectGetter, final Function<X, byte[]> converter
    ) {
        return t -> Optional.of(t)
                .map(objectGetter)
                .map(converter)
                .orElse(NO_BYTES);
    }

    public static <T, E extends Enum<E>> Function<T, byte[]> fromEnum(final Function<T, E> enumGetter) {
        return t -> Optional.of(t)
                .map(enumGetter)
                .map(Enum::name)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .orElse(NO_BYTES);
    }

    public static <T> Function<T, byte[]> fromInt(final Function<T, Integer> intGetter) {
        return t -> ByteBuffer.allocate(Integer.BYTES)
                .putInt(intGetter.apply(t))
                .array();
    }

    public static <T> Function<T, byte[]> fromShort(final Function<T, Short> getShorty) {
        return t -> ByteBuffer.allocate(Short.BYTES)
                .putShort(getShorty.apply(t))
                .array();
    }
}
