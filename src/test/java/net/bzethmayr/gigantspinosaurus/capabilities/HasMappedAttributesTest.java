package net.bzethmayr.gigantspinosaurus.capabilities;

import net.zethmayr.fungu.test.TestHelper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes.attributesFrom;
import static net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes.deepCopyMappedAttributes;
import static org.junit.jupiter.api.Assertions.*;

class HasMappedAttributesTest {

    private static SequencedMap<String, byte[]> some(final int count) {
        final SequencedMap<String, byte[]> map = new LinkedHashMap<>(count);
        IntStream.range(0, count).forEach(x -> {
            map.putLast(TestHelper.randomString(6, "a", "z"),
                    TestHelper.randomString(64).getBytes(StandardCharsets.UTF_8));
        });
        return map;
    }

    private record MapTestFakes(SequencedMap<String, byte[]> original, SequencedSet<String> keys, String sampleKey, byte[] originalSample) {
        static MapTestFakes someFakes(final int count) {
            var original = some(count);
            var keys = new LinkedHashSet<>(original.keySet());
            var sampleKey = keys.getFirst();
            byte[] sample = original.get(sampleKey);
            var originalSample = Arrays.copyOf(sample, sample.length);
            return new MapTestFakes(original, keys, sampleKey, originalSample);
        }
    }

    private static MapTestFakes fakes(final int count) {
        return MapTestFakes.someFakes(count);
    }

    @Test
    void deepCopyMappedAttributes_givenAcceptorAndEmptyMap_populatesDistinctEqualMap() {
        var fakes = fakes(4);
        final SequencedMap<String, byte[]> target = new LinkedHashMap<>();

        deepCopyMappedAttributes(target::put, fakes.original);

        assertNotEquals(fakes.original, target);
        final byte[] resultSample = target.get(fakes.sampleKey);
        assertArrayEquals(fakes.originalSample, resultSample);
        assertNotSame(fakes.originalSample, resultSample);
        fakes.keys.forEach(k -> assertTrue(target.containsKey(k)));
    }

    @Test
    void deepCopyMappedAttributes_givenMapAndNames_returnsDistinctEqualMap() {
        var fakes = fakes(3);

        var result = deepCopyMappedAttributes(fakes.original);

        assertNotEquals(fakes.original, result);
        final byte[] resultSample = result.get(fakes.sampleKey);
        assertArrayEquals(fakes.originalSample, resultSample);
        assertNotSame(fakes.originalSample, resultSample);
        fakes.keys.forEach(k -> assertTrue(result.containsKey(k)));
    }

    @Test
    void deepCopyMappedAttributes_whenInstanceGivenNames_returnsEquivalentMap() {
        var fakes = fakes(5);
        final HasMappedAttributes underTest = attributesFrom(fakes.original::get);

        var result = underTest.deepCopyMappedAttributes(fakes.keys);

        final byte[] resultSample = result.get(fakes.sampleKey);
        assertArrayEquals(fakes.originalSample, resultSample);
        assertNotSame(fakes.originalSample, resultSample);
        fakes.keys.forEach(k -> assertTrue(result.containsKey(k)));
    }

    @Test
    void deepCopyMappedAttributes_whenInstanceGivenAcceptorAndNames_populatesEquivalentMap() {
        var fakes = fakes(4);
        final HasMappedAttributes underTest = attributesFrom(fakes.original::get);
        final Map<String, byte[]> target = new HashMap<>();

        underTest.deepCopyMappedAttributes(target::put, fakes.keys);

        final byte[] resultSample = target.get(fakes.sampleKey);
        assertArrayEquals(fakes.originalSample, resultSample);
        assertNotSame(fakes.originalSample, resultSample);
        fakes.keys.forEach(k -> assertTrue(target.containsKey(k)));
    }
}