package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanPhysicalDeviceSelection.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.lwjgl.system.MemoryStack.stackPush;

class VulkanPhysicalDeviceSelectionTest {

    @Test
    void allPhysicalDevices_givenInstanceAndStack_returnsBuffer() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            hasInstance.withInstance(i -> {
                final PointerBuffer allDevices = allPhysicalDevices(i, stack);

                assertNotNull(allDevices);
            });
        }
    }

    @Test
    void selectPhysicalDevice_givenInstanceAndStack_whenAllScoresEqual_findsFirstDevice() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            final VkPhysicalDevice result = hasInstance.fromInstance(i -> selectPhysicalDevice(i, stack,
                    noComputeQueue(0), discreteBonus(0),
                    seeDeviceNames(System.out::println)));

            assertNotNull(result);
        }
    }

    @Test
    void selectPhysicalDevice_givenInstanceAndStack_whenAllDevicesRejected_throws() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            hasInstance.withInstance(i -> {
                final IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                        selectPhysicalDevice(i, stack, m -> -1));

                assertThat(thrown.getLocalizedMessage().toLowerCase(),
                        stringContainsInOrder("no", "devices"));
            });
        }
    }
}
