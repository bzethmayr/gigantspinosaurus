package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import java.util.List;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanInstanceCreation.allExtensionNames;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanInstanceCreation.allLayerNames;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class VulkanInstanceCreationTest {
    @Test
    void allLayerNames_givenStack_returnsLayerNames() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = allLayerNames(stack);
        }

        assertThat(result, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void allLayerNames_givenStackAndFilter_returnsLayerName() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = allLayerNames(stack, "VK_LAYER_KHRONOS_validation"::equals);
        }

        assertThat(result, hasSize(lessThanOrEqualTo(1)));
    }

    @Test
    void allExtensionNames_givenStack_returnsExtensionNames() {
        final List<String> extensionNames;
        try (final MemoryStack stack = stackPush()) {
            extensionNames = allExtensionNames(stack);
        }

        assertThat(extensionNames, hasSize(greaterThanOrEqualTo(0)));
    }

}
