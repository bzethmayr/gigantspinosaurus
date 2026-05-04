package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.CloseableFactory.closeable;
import static org.lwjgl.vulkan.VK10.*;

class PhysicalDeviceMetadata implements AutoCloseable {
    private final ClosingChain close;
    private final VkExtensionProperties.Buffer vkDeviceExtensions;
    private final VkPhysicalDeviceMemoryProperties vkMemoryProperties;
    private final VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures;
    private final VkPhysicalDeviceProperties vkPhysicalDeviceProperties;
    private final VkQueueFamilyProperties.Buffer vkQueueFamilyProps;

    PhysicalDeviceMetadata(final MemoryStack stack, final VkPhysicalDevice physicalDevice) {
        ClosingChain chain = null;
        try {
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
            vkGetPhysicalDeviceProperties(physicalDevice, vkPhysicalDeviceProperties);
            chain = new ClosingChain(closeable(vkPhysicalDeviceProperties, Struct::free));
            final IntBuffer countBuffer = stack.mallocInt(1);

            checkVk(vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, countBuffer, null),
                    "counting device extensions");
            vkDeviceExtensions = VkExtensionProperties.calloc(countBuffer.get(0));
            checkVk(vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, countBuffer, vkDeviceExtensions),
                    "getting device extensions");
            chain = chain.link(closeable(vkDeviceExtensions, CustomBuffer::free));

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, countBuffer, null);
            vkQueueFamilyProps = VkQueueFamilyProperties.calloc(countBuffer.get(0));
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, countBuffer, vkQueueFamilyProps);
            chain = chain.link(closeable(vkQueueFamilyProps, CustomBuffer::free));

            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
            vkGetPhysicalDeviceFeatures(physicalDevice, vkPhysicalDeviceFeatures);
            chain = chain.link(closeable(vkPhysicalDeviceFeatures, Struct::free));

            vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
            vkGetPhysicalDeviceMemoryProperties(physicalDevice, vkMemoryProperties);
            chain = chain.link(closeable(vkMemoryProperties, Struct::free));
        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        close = chain;
    }

    @Override
    public void close() {
        close.close();
    }

    VkPhysicalDeviceProperties device() {
        return vkPhysicalDeviceProperties;
    }

    VkQueueFamilyProperties.Buffer queueFamilies() {
        return vkQueueFamilyProps;
    }

    VkPhysicalDeviceFeatures features() {
        return vkPhysicalDeviceFeatures;
    }

    VkExtensionProperties.Buffer extensions() {
        return vkDeviceExtensions;
    }

    VkPhysicalDeviceMemoryProperties memory() {
        return vkMemoryProperties;
    }
}
