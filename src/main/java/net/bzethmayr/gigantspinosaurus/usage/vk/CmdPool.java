package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.capabilities.Resettable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseUnsupported;
import static org.lwjgl.vulkan.VK10.*;

class CmdPool implements AutoCloseable, Resettable {
    private final boolean resettable;
    private final VkDevice logicalDevice;
    private final long pool;

    public CmdPool(final MemoryStack stack, final VkDevice logicalDevice, final int queueFamily, final boolean resettable) {
        this.logicalDevice = logicalDevice;
        var cmdPoolInfo = VkCommandPoolCreateInfo.calloc(stack)
                .sType$Default()
                .queueFamilyIndex(queueFamily);
        this.resettable = resettable;
        if (resettable) {
            cmdPoolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        }

        LongBuffer lp = stack.mallocLong(1);
        checkVk(vkCreateCommandPool(logicalDevice, cmdPoolInfo, null, lp),
                "Failed to create command pool");

        pool = lp.get(0);
    }

    @Override
    public void close() {
        vkDestroyCommandPool(logicalDevice, pool, null);
    }

    @Override
    public void reset() {
        if (!resettable) throw becauseUnsupported("pool is not resettable");

        vkResetCommandPool(logicalDevice, pool, 0);
    }
}
