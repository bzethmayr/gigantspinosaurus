package net.bzethmayr.gigantspinosaurus.capabilities;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Key-value attribute access for transport-layer data.
 *
 * <p>The single abstract method {@link #getAttributeValue(String)} returns the
 * raw bytes for a named attribute, or {@code null} if the attribute is absent.
 * Composition methods {@link #prefixWith(HasMappedAttributes)} and
 * {@link #suffixWith(HasMappedAttributes)} let callers layer override and
 * default contexts without mutation.
 *
 * <p>Utility methods
 * ({@link #deepCopyMappedAttributes(BiConsumer, SequencedSet)} and overloads)
 * provide safe, value-copied transfer between attribute sources and sinks.
 */
@FunctionalInterface
public interface HasMappedAttributes {
    /**
     * Attribute access interface.
     * @param attributeName the attribute name
     * @return the attribute value, null, or throws.
     * @see Map#get(Object)
     */
    byte[] getAttributeValue(final String attributeName);

    static HasMappedAttributes attributesFrom(final HasMappedAttributes duck) {
        return duck;
    }

    static void deepCopyMappedAttributes(
            final Function<String, byte[]> attributeToValue,
            final BiConsumer<String, byte[]> attributeAcceptor,
            final SequencedSet<String> attributeNames
    ) {
        for (final String attribute : attributeNames) {
            final byte[] value = attributeToValue.apply(attribute);
            if (value != null) {
                attributeAcceptor.accept(attribute, Arrays.copyOf(value, value.length));
            }
        }
    }

    /**
     * Copies to a map.
     * @param attributeToValue an attribute accessor
     * @param attributeNames the names to copy
     * @return an attribute map.
     */
    static SequencedMap<String, byte[]> deepCopyMappedAttributes(
            final Function<String, byte[]> attributeToValue,
            final SequencedSet<String> attributeNames
    ) {
        final SequencedMap<String, byte[]> copy = new LinkedHashMap<>(attributeNames.size());
        deepCopyMappedAttributes(attributeToValue, copy::putLast, attributeNames);
        return copy;
    }

    default HasMappedAttributes prefixWith(final HasMappedAttributes overridingContext) {
        return s -> Optional.of(s)
                .map(overridingContext::getAttributeValue)
                .orElseGet(() -> getAttributeValue(s));
    }

    default HasMappedAttributes suffixWith(final HasMappedAttributes defaultingContext) {
        return s -> Optional.of(s)
                .map(this::getAttributeValue)
                .orElseGet(() -> defaultingContext.getAttributeValue(s));
    }

    default void deepCopyMappedAttributes(
            final BiConsumer<String, byte[]> attributeAcceptor,
            final SequencedSet<String> attributeNames
    ) {
        deepCopyMappedAttributes(this::getAttributeValue, attributeAcceptor, attributeNames);
    }

    default SequencedMap<String, byte[]> deepCopyMappedAttributes(final SequencedSet<String> attributeNames) {
        return deepCopyMappedAttributes(this::getAttributeValue, attributeNames);
    }

    static SequencedMap<String, byte[]> deepCopyMappedAttributes(final SequencedMap<String, byte[]> attributeToValue) {
        return deepCopyMappedAttributes(attributeToValue::get, attributeToValue.sequencedKeySet());
    }

    static void deepCopyMappedAttributes(
            final BiConsumer<String, byte[]> attributeAcceptor,
            final SequencedMap<String, byte[]> attributeToValue
    ) {
        deepCopyMappedAttributes(attributeToValue::get, attributeAcceptor, attributeToValue.sequencedKeySet());
    }
}
