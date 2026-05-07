package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.indexOfMaxScorePassing;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.*;

class QueueSelection {
    private QueueSelection() {
        throw becauseStaticsOnly();
    }

    @SafeVarargs
    static VulkanQueue selectQueue(
            final MemoryStack stack,
            final PhysicalDeviceMetadata metadata,
            final VkDevice logicalDevice,
            final int queueIndex,
            final ToIntFunction<VkQueueFamilyProperties>... scorers) {
        final VkQueueFamilyProperties.Buffer queueFamilies = metadata.queueFamilies();
        final int numFamilies = queueFamilies.capacity();
        final int[] scores = new int[numFamilies];
        for (int i = 0; i < numFamilies; i++) {
            final VkQueueFamilyProperties familyProps = queueFamilies.get(i);
            scores[i] = Stream.of(scorers).mapToInt(f -> f.applyAsInt(familyProps)).sum();
        }
        final int selectedFamily = indexOfMaxScorePassing("No compatible queues", scores);
        final PointerBuffer queueBuf = stack.mallocPointer(1);
        vkGetDeviceQueue(logicalDevice, selectedFamily, queueIndex, queueBuf);
        final long queue = queueBuf.get(0);
        return new VulkanQueue(selectedFamily, new VkQueue(queue, logicalDevice));
    }

    static ToIntFunction<VkQueueFamilyProperties> computeQueueOr(final int penalty) {
        return qp -> (qp.queueFlags() & VK_QUEUE_COMPUTE_BIT) != 0
                ? 0
                : penalty;
    }

    static ToIntFunction<VkQueueFamilyProperties> dedicatedCompute(final int bonus) {
        return qp -> {
            final int flags = qp.queueFlags();
            return (flags & VK_QUEUE_COMPUTE_BIT) != 0 && (flags & VK_QUEUE_GRAPHICS_BIT) == 0
                    ? bonus
                    : 0;
        };
    }

    static ToIntFunction<VkQueueFamilyProperties> countBonus() {
        return qp -> 32 - Integer.numberOfLeadingZeros(qp.queueCount());
    }
}
