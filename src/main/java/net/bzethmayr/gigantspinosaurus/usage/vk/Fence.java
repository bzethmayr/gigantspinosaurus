package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.capabilities.Resettable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Fence implements AutoCloseable, Resettable {
    private final VkDevice logicalDevice;
    private final long vkFence;

    public Fence(VkDevice logicalDevice, boolean signaled) {
        this.logicalDevice = logicalDevice;
        try (final MemoryStack stack = stackPush()) {
            var fenceCreateInfo = VkFenceCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(signaled ? VK_FENCE_CREATE_SIGNALED_BIT : 0);

            LongBuffer lp = stack.mallocLong(1);
            checkVk(vkCreateFence(logicalDevice, fenceCreateInfo, null, lp),
                    "creating a fence");
            vkFence = lp.get(0);
        }
    }

    @Override
    public void close() {
        vkDestroyFence(logicalDevice, vkFence, null);
    }

    public void fenceWait() {
        vkWaitForFences(logicalDevice, vkFence, true, Long.MAX_VALUE);
    }

    public long getVkFence() {
        return vkFence;
    }

    @Override
    public void reset() {
        vkResetFences(logicalDevice, vkFence);
    }

}
