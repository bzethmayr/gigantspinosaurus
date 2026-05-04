package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VulkanRootTest {

    @Test
    void instance_exposesInstanceToConsumer() {
        try (final VulkanRoot underTest = new VulkanRoot()) {
            assertNotNull(underTest.instance());
        }
    }
}