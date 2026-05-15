package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.junit.jupiter.api.Test;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;

import static net.bzethmayr.gigantspinosaurus.usage.vk.PhysicalDeviceSelection.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.lwjgl.system.MemoryStack.stackPush;

class PhysicalDeviceSelectionTest {

    @Test
    void allPhysicalDevices_givenInstanceAndStack_returnsBuffer() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            final PointerBuffer allDevices = allPhysicalDevices(stack, hasInstance.instance());

            assertNotNull(allDevices);
        }
    }

    @Test
    void selectPhysicalDevice_givenInstanceAndStack_whenAllScoresEqual_findsFirstDevice() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            final VkPhysicalDevice result = selectPhysicalDevice(stack, hasInstance.instance(),
                    noComputeQueue(0), discreteBonus(0),
                    seeDeviceNames(System.out::println));

            assertNotNull(result);
        }
    }

    @Test
    void selectPhysicalDevice_givenInstanceAndStack_whenAllDevicesRejected_throws() {
        try (final VulkanRoot hasInstance = new VulkanRoot(); final MemoryStack stack = stackPush()) {
            final IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                    selectPhysicalDevice(stack, hasInstance.instance(), m -> -1));

            assertThat(thrown.getLocalizedMessage().toLowerCase(),
                    stringContainsInOrder("no", "devices"));
        }
    }
}
