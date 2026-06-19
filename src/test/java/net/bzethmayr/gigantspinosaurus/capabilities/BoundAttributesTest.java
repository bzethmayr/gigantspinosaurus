package net.bzethmayr.gigantspinosaurus.capabilities;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;

import static net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes.AttributeBinder.binder;
import static org.junit.jupiter.api.Assertions.*;

class BoundAttributesTest {

    private static byte[] expectedValue() {
        return new byte[]{42};
    }

    @Test
    void getBoundValue_returnsValueFromAccessor() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key));

        final byte[] result = underTest.getBoundValue("key", new FakeMapped());

        assertArrayEquals(expectedValue(), result);
    }

    @Test
    void getBoundValue_whenFieldAbsent_returnsNull() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key));

        final byte[] result = underTest.getBoundValue("other", new FakeMapped());

        assertNull(result);
    }

    @Test
    void getBoundValueOrDelegate_presentField_returnsBoundValue() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key));
        final HasMappedAttributes delegate = n -> new byte[]{2};

        final byte[] result = underTest.getBoundValueOrDelegate(
                "key", new FakeMapped(new byte[]{1}), delegate);

        assertArrayEquals(new byte[]{1}, result);
    }

    @Test
    void getBoundValueOrDelegate_absentField_delegates() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key));
        final HasMappedAttributes delegate = n -> new byte[]{2};

        final byte[] result = underTest.getBoundValueOrDelegate(
                "other", new FakeMapped(new byte[]{1}), delegate);

        assertArrayEquals(new byte[]{2}, result);
    }

    @Test
    void getBoundValueOrDelegate_absentFieldWithNullDelegate_returnsNull() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key));

        final byte[] result = underTest.getBoundValueOrDelegate(
                "other", new FakeMapped(), n -> null);

        assertNull(result);
    }

    @Test
    void fieldNames_returnsUnmodifiableSequencedSet() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("a", t -> null),
                m -> m.put("b", t -> null));

        final SequencedSet<String> names = underTest.fieldNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
        assertThrows(UnsupportedOperationException.class, () -> names.add("c"));
    }

    @Test
    void multipleBinders_allApplied() {
        final BoundAttributes<FakeMapped> underTest = new BoundAttributes<>(
                FakeMapped.class,
                m -> m.put("key", FakeMapped::key),
                m -> m.put("y", t -> new byte[]{2}));

        assertArrayEquals(expectedValue(), underTest.getBoundValue("key", new FakeMapped()));
        assertArrayEquals(new byte[]{2}, underTest.getBoundValue("y", new FakeMapped()));
    }

    @Test
    void binder_staticFactory_wrapsConsumer() {
        final Map<String, Function<FakeMapped, byte[]>> map = new LinkedHashMap<>();
        final BoundAttributes.AttributeBinder<FakeMapped> attrBinder = binder(m -> m.put("z", t -> new byte[]{3}));
        attrBinder.accept(map);
        assertTrue(map.containsKey("z"));
    }

    private record FakeMapped(byte[] key) implements HasMappedAttributes {

        public FakeMapped() {
            this(expectedValue());
        }

        @Override
        public byte[] getAttributeValue(final String attributeName) {
            return null;
        }
    }
}
