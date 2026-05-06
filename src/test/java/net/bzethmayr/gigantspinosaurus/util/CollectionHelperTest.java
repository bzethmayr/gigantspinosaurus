package net.bzethmayr.gigantspinosaurus.util;

import net.zethmayr.fungu.Fork;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.refilter;
import static net.zethmayr.fungu.ForkFactory.overPrior;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CollectionHelperTest {

    @Test
    void adds_givenKeyAndValues_returnsPutter() {
        final Map<String, Integer> collects = new HashMap<>();
        final Consumer<Map<String, Integer>> underTest = adds("a", 1);

        underTest.accept(collects);

        assertThat(collects, hasEntry("a", 1));
    }

    @Test
    void toSequencedMap_givenSequencedItems_collectsInSequence() {

        final Map<Integer, Integer> result = IntStream.range(0, 3).boxed()
                .map(overPrior())
                .collect(CollectionHelper.toSequencedMap(Fork::top, Fork::bottom));

        assertThat(result.values(), contains(null, 0, 1));
    }

    @Test
    void refilter_givenStrings_returnsPredicates() {
        final List<String> original = List.of("a", "b", "d");
        final int size = original.size();

        final Predicate<String>[] result = refilter(original);

        assertThat(result, arrayWithSize(size));
        for (int i = 0; i < size; i++) {
            assertTrue(result[i].test(original.get(i)));
            assertFalse(result[i].test(original.get((i + 1) % size)));
            assertFalse(result[i].test(original.get((i + 2) % size)));
        }
    }
}