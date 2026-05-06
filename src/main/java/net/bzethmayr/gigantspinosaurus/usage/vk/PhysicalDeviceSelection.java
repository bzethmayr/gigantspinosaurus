package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.indexOfMaxScorePassing;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.*;

final class PhysicalDeviceSelection {
    private PhysicalDeviceSelection() {
        throw becauseStaticsOnly();
    }

    static PointerBuffer allPhysicalDevices(
            final MemoryStack stack,
            final VkInstance instance
    ) {
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
            final MemoryStack stack,
            final VkInstance instance,
            final ToIntFunction<PhysicalDeviceMetadata>... scorers
    ) {
        final PointerBuffer allDevices = allPhysicalDevices(stack, instance);
        final int numDevices = allDevices.capacity();
        final int[] scores = new int[numDevices];
        final VkPhysicalDevice[] candidates = new VkPhysicalDevice[numDevices];

        for (int i = 0; i < numDevices; i++) {
            candidates[i] = new VkPhysicalDevice(allDevices.get(i), instance);
            try (final PhysicalDeviceMetadata metadata = new PhysicalDeviceMetadata(stack, candidates[i])) {
                scores[i] = Stream.of(scorers).mapToInt(f -> f.applyAsInt(metadata)).sum();
            }
        }

        return candidates[indexOfMaxScorePassing("No compatible devices", scores)];
    }

    static ToIntFunction<PhysicalDeviceMetadata> noComputeQueue(final int penalty) {
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

    static ToIntFunction<PhysicalDeviceMetadata> discreteBonus(final int bonus) {
        return m -> switch (m.device().deviceType()) {
            case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> bonus;
            case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU,
                 VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU -> bonus / 2;
            default -> 0;
        };
    }

    static ToIntFunction<PhysicalDeviceMetadata> seeDeviceNames(final Consumer<String> acceptsNames) {
        return m -> {
            acceptsNames.accept(m.device().deviceNameString());
            return 0;
        };
    }
}
