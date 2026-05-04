package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.*;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.refilter;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;

final class VulkanLogicalDeviceCreation {

    static VkDevice configureLogicalDevice(
            final MemoryStack stack,
            final VkPhysicalDevice physicalDevice,
            final PhysicalDeviceMetadata physicalMetadata,
            final List<String> extensions
    ) {
        final VkQueueFamilyProperties.Buffer queuePropsBuf = physicalMetadata.queueFamilies();
        final int numQueueFamilies = queuePropsBuf.capacity();
        final VkDeviceQueueCreateInfo.Buffer queueSpecsBuf = VkDeviceQueueCreateInfo.calloc(numQueueFamilies, stack);
        for (int i = 0; i < numQueueFamilies; i++) {
            final FloatBuffer queuePriorities = stack.callocFloat(queuePropsBuf.get(i).queueCount());
            queueSpecsBuf.get(i)
                    .sType$Default()
                    .queueFamilyIndex(i)
                    .pQueuePriorities(queuePriorities);
        }
        final VkDeviceCreateInfo deviceSpec = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
                .ppEnabledExtensionNames(asciiNamesFlippedFrom(stack,
                        deviceExtensionNames(stack, physicalDevice, refilter(extensions))))
                .pQueueCreateInfos(queueSpecsBuf);
        final PointerBuffer deviceBuf = stack.mallocPointer(1);
        checkVk(vkCreateDevice(physicalDevice, deviceSpec, null, deviceBuf),
                "logical device creation");
        return new VkDevice(deviceBuf.get(0), physicalDevice, deviceSpec);
    }

    @SafeVarargs
    static List<String> deviceExtensionNames(
            final MemoryStack stack,
            final VkPhysicalDevice physicalDevice,
            final Predicate<String>... limitTo) {
        final IntBuffer extensionBuf = stack.callocInt(1);
        vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionBuf, null);
        final int numExtensions = extensionBuf.get(0);
        final List<String> extensions = filteredList(numExtensions, limitTo);
        try (final VkExtensionProperties.Buffer propsBuf = VkExtensionProperties.calloc(numExtensions)) {
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, extensionBuf, propsBuf);
            final Predicate<String> filter = optionalAny(limitTo);
            for (int i = 0; i < numExtensions; i++) {
                final VkExtensionProperties props = propsBuf.get(i);
                final String extensionName = props.extensionNameString();
                if (filter.test(extensionName)) {
                    extensions.add(extensionName);
                }
            }
        }
        return extensions;
    }
}
