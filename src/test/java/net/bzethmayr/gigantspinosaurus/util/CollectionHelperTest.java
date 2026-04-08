package net.bzethmayr.gigantspinosaurus.util;

import net.zethmayr.fungu.Fork;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;
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
}