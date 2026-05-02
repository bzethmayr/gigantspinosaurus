package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lwjgl.vulkan.VKCapabilitiesInstance;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class VulkanRootTest {

    @Test
    void withInstance_exposesInstanceToConsumer() {
        try (final VulkanRoot underTest = new VulkanRoot()) {

            underTest.withInstance(Assertions::assertNotNull);
        }
    }

    @Test
    void fromInstance_exposesInstanceToFunction() {
        try (final VulkanRoot underTest = new VulkanRoot()) {

            final VKCapabilitiesInstance result = underTest.fromInstance(i -> i.getCapabilities());

            assertNotEquals(0, result.apiVersion);
        }
    }
}