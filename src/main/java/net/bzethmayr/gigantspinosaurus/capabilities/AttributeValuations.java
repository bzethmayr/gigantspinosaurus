package net.bzethmayr.gigantspinosaurus.capabilities;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseNotInstantiable;

/**
 * Canonical attribute serializers and serializer wrapper.
 */
public final class AttributeValuations {
    private AttributeValuations() {
        throw becauseNotInstantiable();
    }

    private static final byte[] NO_BYTES = new byte[0];

    /**
     * {@code byte[]} serializer / identity.
     * @param byteGetter retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type.
     */
    public static <T> Function<T, byte[]> fromBytes(final Function<T, byte[]> byteGetter) {
        return byteGetter;
    }

    /**
     * {@code long} serializer.
     * @param longGetter retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type.
     */
    public static <T> Function<T, byte[]> fromLong(final Function<T, Long> longGetter) {
        return t -> ByteBuffer.allocate(Long.BYTES)
                .putLong(longGetter.apply(t))
                .array();
    }

    /**
     * {@code double} serializer.
     * @param doubleGetter retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type.
     */
    public static <T> Function<T, byte[]> fromDouble(final Function<T, Double> doubleGetter) {
        return t -> ByteBuffer.allocate(Double.BYTES)
                .putDouble(doubleGetter.apply(t))
                .array();
    }

    /**
     * {@code enum} serializer.
     * @param enumGetter retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type.
     * @param <E> the enum type.
     */
    public static <T, E extends Enum<E>> Function<T, byte[]> fromEnum(final Function<T, E> enumGetter) {
        return t -> Optional.of(t)
                .map(enumGetter)
                .map(Enum::name)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .orElse(NO_BYTES);
    }

    /**
     * {@code int} serializer.
     * @param intGetter retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type
     */
    public static <T> Function<T, byte[]> fromInt(final Function<T, Integer> intGetter) {
        return t -> ByteBuffer.allocate(Integer.BYTES)
                .putInt(intGetter.apply(t))
                .array();
    }

    /**
     * {@code short} serializer.
     * @param getShorty retrieves the attribute value.
     * @return an attribute serializer.
     * @param <T> the source type
     */
    public static <T> Function<T, byte[]> fromShort(final Function<T, Short> getShorty) {
        return t -> ByteBuffer.allocate(Short.BYTES)
                .putShort(getShorty.apply(t))
                .array();
    }

    /**
     * Custom serialization wrapper.
     * @param objectGetter retrieves the attribute value.
     * @param converter produces the canonical form.
     * @return an attribute serializer.
     * @param <T> the source type
     * @param <X> the attribute type
     */
    public static <T, X> Function<T, byte[]> fromConverted(
            final Function<T, X> objectGetter, final Function<X, byte[]> converter
    ) {
        return t -> Optional.of(t)
                .map(objectGetter)
                .map(converter)
                .orElse(NO_BYTES);
    }
}
