package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.system.MemoryStack;

import java.util.List;

import static net.bzethmayr.gigantspinosaurus.usage.vk.LogicalDeviceCreation.deviceExtensionNames;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.PORTABILITY_EXTENSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.lwjgl.system.MemoryStack.stackPush;

class LogicalDeviceCreationTest {

    @Test
    void deviceExtensionNames_givenInstanceAndDevice_returnsAllNames() {
        try (MemoryStack stack = stackPush(); VulkanRoot hasDevice = new VulkanRoot()) {

            final List<String> result = deviceExtensionNames(
                    stack, hasDevice.physicalDevice());

            assertThat(result, hasSize(greaterThanOrEqualTo(0)));
        }
    }

    @Test
    void deviceExtensionNames_givenInstanceDeviceAndLimit_returnsAtMostLimit() {
        try (MemoryStack stack = stackPush(); VulkanRoot hasDevice = new VulkanRoot()) {

            final List<String> result = deviceExtensionNames(
                    stack, hasDevice.physicalDevice(), PORTABILITY_EXTENSION::equals);

            assertThat(result, hasSize(lessThanOrEqualTo(1)));
        }
    }
}
