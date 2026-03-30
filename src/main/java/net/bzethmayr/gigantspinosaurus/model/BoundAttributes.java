package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSequencedMap;
import static java.util.Collections.unmodifiableSequencedSet;
import static net.zethmayr.fungu.UponHelper.upon;

public final class BoundAttributes<T extends HasMappedAttributes> {
    private static final HasMappedAttributes EMPTY = s -> null;
    private final SequencedMap<String, Function<T, byte[]>> accessors;
    private final SequencedSet<String> fieldNames;

    @SafeVarargs
    public BoundAttributes(final Consumer<Map<String, Function<T, byte[]>>>... binders) {
        accessors = unmodifiableSequencedMap(upon(new LinkedHashMap<>(), binders));
        fieldNames = accessors.sequencedKeySet();
    }

    public SequencedSet<String> fieldNames() {
        return fieldNames;
    }

    public SequencedSet<String> fieldNamesExcept(final String... except) {
        final SequencedSet<String> reduced = new LinkedHashSet<>(fieldNames);
        Stream.of(except).forEach(reduced::remove);
        return unmodifiableSequencedSet(reduced);
    }

    public byte[] getBoundValueOrDelegate(final String fieldName, final T binding, final HasMappedAttributes delegate) {
        if (fieldNames.contains(fieldName)) {
            return accessors.get(fieldName).apply(binding);
        } else {
            return delegate.getAttributeValue(fieldName);
        }
    }

    public byte[] getBoundValue(final String fieldName, final T binding) {
        return getBoundValueOrDelegate(fieldName, binding, EMPTY);
    }
}
