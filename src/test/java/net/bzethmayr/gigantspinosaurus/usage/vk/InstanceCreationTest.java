package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import java.util.List;

import static net.bzethmayr.gigantspinosaurus.usage.vk.InstanceCreation.instanceExtensionNames;
import static net.bzethmayr.gigantspinosaurus.usage.vk.InstanceCreation.instanceLayerNames;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class InstanceCreationTest {
    @Test
    void instanceLayerNames_givenStack_returnsLayerNames() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = instanceLayerNames(stack);
        }

        assertThat(result, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void instanceLayerNames_givenStackAndFilter_returnsLayerName() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = instanceLayerNames(stack, "VK_LAYER_KHRONOS_validation"::equals);
        }

        assertThat(result, hasSize(lessThanOrEqualTo(1)));
    }

    @Test
    void instanceExtensionNames_givenStack_returnsExtensionNames() {
        final List<String> extensionNames;
        try (final MemoryStack stack = stackPush()) {
            extensionNames = instanceExtensionNames(stack);
        }

        assertThat(extensionNames, hasSize(greaterThanOrEqualTo(0)));
    }

}
