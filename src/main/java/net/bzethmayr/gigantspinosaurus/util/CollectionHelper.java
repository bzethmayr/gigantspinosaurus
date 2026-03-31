package net.bzethmayr.gigantspinosaurus.util;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

public final class CollectionHelper {

    public static <K, V> Consumer<Map<K, V>> adds(final K key, final V value) {
        return m -> m.put(key, value);
    }

    public static <S, K, V> Collector<S, ?, SequencedMap<K, V>> toSequencedMap(
            Function<? super S, ? extends K> keyFn,
                   Function<? super S, ? extends V> valueFn) {
        return Collector.of(
                LinkedHashMap::new,
                (m, s) -> m.putLast(keyFn.apply(s), valueFn.apply(s)),
                (left, right) -> {
                    right.forEach(left::putLast);
                    return left;
                },
                m -> null,
                Collector.Characteristics.IDENTITY_FINISH
        );
    }
}
