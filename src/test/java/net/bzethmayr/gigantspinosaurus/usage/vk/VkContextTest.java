package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VKCapabilitiesInstance;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.system.MemoryStack.stackPush;

class VkContextTest {

    @Test
    void allLayerNames_givenStack_returnsLayerNames() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = VkContext.allLayerNames(stack);
        }

        assertThat(result, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void allLayerNames_givenStackAndFilter_returnsLayerName() {
        final List<String> result;
        try (final MemoryStack stack = stackPush()) {
            result = VkContext.allLayerNames(stack, "VK_LAYER_KHRONOS_validation"::equals);
        }

        assertThat(result, hasSize(lessThanOrEqualTo(1)));
    }

    @Test
    void allExtensionNames_givenStack_returnsExtensionNames() {
        final List<String> extensionNames;
        try (final MemoryStack stack = stackPush()) {
            extensionNames = VkContext.allExtensionNames(stack);
        }

        assertThat(extensionNames, hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void withInstance_exposesInstanceToConsumer() {
        try (final VkContext underTest = new VkContext()) {

            underTest.withInstance(Assertions::assertNotNull);
        }
    }

    @Test
    void fromInstance_exposesInstanceToFunction() {
        try (final VkContext underTest = new VkContext()) {

            final VKCapabilitiesInstance result = underTest.fromInstance(i -> i.getCapabilities());

            assertNotEquals(0, result.apiVersion);
        }
    }
}