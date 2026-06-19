package net.bzethmayr.gigantspinosaurus.capabilities;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class VersionedTest {

    @Test
    @SuppressWarnings("unchecked")
    void addsVersion_putsVersionAttribute() {
        final var binder = (BoundAttributes.AttributeBinder<Versioned>) (Object) Versioned.addsVersion();
        final Map<String, Function<Versioned, byte[]>> map = new LinkedHashMap<>();

        binder.accept(map);

        assertTrue(map.containsKey("ver"));
        final byte[] bytes = map.get("ver").apply(new FakeVersioned((short) 42));
        assertArrayEquals(new byte[]{0, 42}, bytes);
    }

    @Test
    @SuppressWarnings("unchecked")
    void addsVersion_rejectsMapWithEntries() {
        final var binder = (BoundAttributes.AttributeBinder<Versioned>) (Object) Versioned.addsVersion();
        final Map<String, Function<Versioned, byte[]>> map = new LinkedHashMap<>();
        binder.accept(map);
        map.put("other", t -> new byte[0]);

        assertThrows(IllegalStateException.class, () -> binder.accept(map));
    }

    private record FakeVersioned(short v) implements Versioned {
        @Override
        public short version() {
            return v;
        }

        @Override
        public byte[] getAttributeValue(final String attributeName) {
            return null;
        }
    }
}
