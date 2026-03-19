package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes;

import java.util.Arrays;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Factories for the transport type.
 */
public final class AttributePlumbing {

    private AttributePlumbing() {
        throw new UnsupportedOperationException();
    }

    public static Function<HasMappedAttributes, byte[]> soleContext(
            final HasMappedAttributes context,
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        return x -> wrapped.apply(context);
    }

    public static Function<HasMappedAttributes, byte[]> preContext(
            final HasMappedAttributes context,
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        return ctx -> wrapped.apply(ctx.prefixWith(context));
    }

    public static Function<HasMappedAttributes, byte[]> postContext(
            final HasMappedAttributes context,
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        return ctx -> wrapped.apply(ctx.suffixWith(context));
    }

    public static Function<HasMappedAttributes, byte[]> surface(final String attributeName) {
        return ctx -> ctx.getAttributeValue(attributeName);
    }

    public static Function<HasMappedAttributes, byte[]> guard(
            final Predicate<HasMappedAttributes> filter,
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        return ctx -> Optional.of(ctx)
                .filter(filter)
                .map(wrapped)
                .orElse(null);
    }

    public static Function<HasMappedAttributes, byte[]> nothing() {
        return ctx -> null;
    }

    public static Function<HasMappedAttributes, byte[]> value(final byte[] value) {
        final byte[] copied = Arrays.copyOf(value, value.length);
        return ctx ->  copied;
    }

    public static Function<HasMappedAttributes, byte[]> cached(
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        final WeakHashMap<HasMappedAttributes, byte[]> cache = new WeakHashMap<>();
        return ctx -> cache.computeIfAbsent(ctx, wrapped);
    }

    public static Function<HasMappedAttributes, byte[]> ephemeral(
            final Function<HasMappedAttributes, byte[]> wrapped
    ) {
        final AtomicReference<Function<HasMappedAttributes, byte[]>> once = new AtomicReference<>();
        once.set(ctx -> {
            final byte[] value = wrapped.apply(ctx);
            once.set(nothing());
            return value;
        });
        return ctx -> once.get().apply(ctx);
    }

    public static Function<HasMappedAttributes, byte[]> mapBytes(
            final Function<HasMappedAttributes, byte[]> wrapped,
            final Function<byte[], byte[]> transform
    ) {
        return ctx -> Optional.of(ctx)
                .map(wrapped)
                .map(transform)
                .orElse(null);
    }

    public static Function<HasMappedAttributes, byte[]> combine(
            final Function<HasMappedAttributes, byte[]> left,
            final Function<HasMappedAttributes, byte[]> right,
            final BiFunction<byte[], byte[], byte[]> combiner
            ) {
        return ctx -> combiner.apply(left.apply(ctx), right.apply(ctx));
    }
}
