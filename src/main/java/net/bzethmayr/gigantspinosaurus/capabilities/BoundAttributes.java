package net.bzethmayr.gigantspinosaurus.capabilities;

import net.zethmayr.fungu.core.declarations.ReuseResults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.unmodifiableSequencedMap;
import static net.zethmayr.fungu.UponHelper.upon;

/**
 * @see net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations for bindings.
 * @param <T>
 */
public final class BoundAttributes<T extends HasMappedAttributes> {
    private static final HasMappedAttributes EMPTY = s -> null;
    private final SequencedMap<String, Function<T, byte[]>> accessors;
    private final SequencedSet<String> fieldNames;

    @SafeVarargs
    public BoundAttributes(final Consumer<Map<String, Function<T, byte[]>>>... binders) {
        accessors = unmodifiableSequencedMap(new LinkedHashMap<>(upon(new LinkedHashMap<>(), binders)));
        fieldNames = accessors.sequencedKeySet();
    }

    @FunctionalInterface
    public interface AttributeBinder<T> extends Consumer<Map<String, Function<T, byte[]>>> {
        static <T> AttributeBinder<T> binder(final Consumer<Map<String, Function<T, byte[]>>> duck) {
            return duck::accept;
        }
    }

    @ReuseResults
    public SequencedSet<String> fieldNames() {
        return fieldNames;
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
