package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.util.Queue;
import java.util.function.ToIntFunction;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseStaticsOnly;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;

class QueueSelection {
    private QueueSelection() {
        throw becauseStaticsOnly();
    }

    @SafeVarargs
    static VkQueue createQueue(final MemoryStack stack, final PhysicalDeviceMetadata metadata, final VkDevice logicalDevice,
                        final ToIntFunction<VkQueueFamilyProperties>... scorers) {

        return null;
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
