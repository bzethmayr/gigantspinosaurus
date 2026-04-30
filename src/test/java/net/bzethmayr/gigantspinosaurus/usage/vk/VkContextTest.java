package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

class VkContextTest {

    @Test
    void allLayerNames_givenStack_returnsLayerNames() {
        final List<String> layerNames;
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            layerNames = VkContext.allLayerNames(stack);
        }

        assertThat(layerNames, hasSize(greaterThanOrEqualTo(0)));
    }
}