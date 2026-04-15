package net.bzethmayr.gigantspinosaurus.capabilities;

import net.zethmayr.fungu.Fork;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;

import static java.util.function.Predicate.not;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.toSequencedMap;
import static net.zethmayr.fungu.ForkFactory.over;

public interface HasCanonicalAttributes extends HasMappedAttributes, Versioned {
    SequencedSet<String> getCanonicalAttributes();

    default SequencedMap<String, byte[]> getCanonicalAttributeValues(final String... excluding) {
        final Set<String> excluded = Set.of(excluding);
        return getCanonicalAttributes().stream()
                .filter(not(excluded::contains))
                .map(over(this::getAttributeValue))
                .collect(toSequencedMap(Fork::top, Fork::bottom));
    }

    default byte[] canonicalBytes(final String... excluding) {
        final SequencedMap<String, byte[]> outMap = getCanonicalAttributeValues(excluding);
        final int keys = outMap.size();
        final int outSize = outMap.keySet().stream().mapToInt(String::length).sum()
                + outMap.values().stream().mapToInt(a -> a.length).sum()
                + 2 * keys
                + 1;
        final ByteBuffer out = ByteBuffer.allocate(outSize);
        out.put((byte) '{');
        for (Map.Entry<String, byte[]> pair : outMap.sequencedEntrySet()) {
            out.put(pair.getKey().getBytes(StandardCharsets.UTF_8));
            out.put((byte) ':');
            out.put(pair.getValue());
            out.put((byte) ',');
        }
        if (keys > 0) {
            out.put(out.position() - 1, (byte) '}');
        } else {
            out.put((byte) '}');
        }
        return out.array();
    }

    @FunctionalInterface
    interface CanonicalDecoder<T> {
        T decode(final ByteBuffer in, CanonizesDecoders decoders);
    }

    @FunctionalInterface
    interface CanonizesDecoders {
        <T> CanonicalDecoder<T> decoderFor(String key);
    }
}
