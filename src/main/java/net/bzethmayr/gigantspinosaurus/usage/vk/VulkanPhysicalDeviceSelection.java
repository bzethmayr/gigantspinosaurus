package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.*;

final class VulkanPhysicalDeviceSelection {
    private VulkanPhysicalDeviceSelection() {
        throw becauseStaticsOnly();
    }

    static class DeviceMetadata implements AutoCloseable {
        private final VkExtensionProperties.Buffer vkDeviceExtensions;
        private final VkPhysicalDeviceMemoryProperties vkMemoryProperties;
        private final VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures;
        private final VkPhysicalDeviceProperties vkPhysicalDeviceProperties;
        private final VkQueueFamilyProperties.Buffer vkQueueFamilyProps;

        private DeviceMetadata(final MemoryStack stack, final VkPhysicalDevice physicalDevice) {
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
            vkGetPhysicalDeviceProperties(physicalDevice, vkPhysicalDeviceProperties);
            final IntBuffer countBuffer = stack.mallocInt(1);

            checkVk(vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, countBuffer, null),
                    "counting device extensions");
            vkDeviceExtensions = VkExtensionProperties.calloc(countBuffer.get(0));
            checkVk(vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, countBuffer, vkDeviceExtensions),
                    "getting device extensions");

            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, countBuffer, null);
            vkQueueFamilyProps = VkQueueFamilyProperties.calloc(countBuffer.get(0));
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, countBuffer, vkQueueFamilyProps);

            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
            vkGetPhysicalDeviceFeatures(physicalDevice, vkPhysicalDeviceFeatures);

            vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
            vkGetPhysicalDeviceMemoryProperties(physicalDevice, vkMemoryProperties);
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

        @Override
        public void close() {
            vkMemoryProperties.free();
            vkPhysicalDeviceFeatures.free();
            vkQueueFamilyProps.free();
            vkDeviceExtensions.free();
            vkPhysicalDeviceProperties.free();
        }
    }

    static PointerBuffer allPhysicalDevices(VkInstance instance, MemoryStack stack) {
        final IntBuffer countBuffer = stack.mallocInt(1);
        checkVk(vkEnumeratePhysicalDevices(instance, countBuffer, null),
                "counting physical devices");
        final int numDevices = countBuffer.get(0);
        final PointerBuffer devices = stack.mallocPointer(numDevices);
        checkVk(vkEnumeratePhysicalDevices(instance, countBuffer, devices),
                "getting physical devices");
        return devices;
    }

    @SafeVarargs
    static VkPhysicalDevice selectPhysicalDevice(
            final VkInstance instance,
            final MemoryStack stack,
            final ToIntFunction<DeviceMetadata>... scorers
    ) {
        final PointerBuffer allDevices = allPhysicalDevices(instance, stack);
        final int numDevices = allDevices.capacity();
        final int[] scores = new int[numDevices];
        final VkPhysicalDevice[] candidates = new VkPhysicalDevice[numDevices];

        for (int i = 0; i < numDevices; i++) {
            candidates[i] = new VkPhysicalDevice(allDevices.get(i), instance);
            try (final DeviceMetadata metadata = new DeviceMetadata(stack, candidates[i])) {
                scores[i] = Stream.of(scorers).mapToInt(f -> f.applyAsInt(metadata)).sum();
            }
        }
        int maxScore = Integer.MIN_VALUE;
        int maxAt = 0;
        for (int i = 0; i < numDevices; i++) {
            if (scores[i] > maxScore) {
                maxAt = i;
                maxScore = scores[i];
            }
        }
        if (maxScore < 0) {
            throw new IllegalStateException("No supported devices");
        }
        return candidates[maxAt];
    }

    static ToIntFunction<DeviceMetadata> noComputeQueue(final int penalty) {
        return m -> {
            final VkQueueFamilyProperties.Buffer queueFamilies = m.queueFamilies();
            final int numFamilies = queueFamilies != null
                    ? queueFamilies.capacity()
                    : 0;
            for (int i = 0; i < numFamilies; i++) {
                if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_COMPUTE_BIT) != 0) {
                    return 0;
                }
            }
            return penalty;
        };
    }

    static ToIntFunction<DeviceMetadata> discreteBonus(final int bonus) {
        return m -> switch (m.device().deviceType()) {
            case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> bonus;
            case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU,
                 VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU -> bonus / 2;
            default -> 0;
        };
    }

    static ToIntFunction<DeviceMetadata> seeDeviceNames(final Consumer<String> acceptsNames) {
        return m -> {
            acceptsNames.accept(m.device().deviceNameString());
            return 0;
        };
    }
}
