package net.bzethmayr.gigantspinosaurus.usage.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

record VulkanQueue(int familyIndex, VkQueue vkQueue) {
    public void waitIdle() {
        vkQueueWaitIdle(vkQueue);
    }

    public void submit(VkCommandBufferSubmitInfo.Buffer commandBuffer, Fence fence) {
        try (final MemoryStack stack = stackPush()) {
            final PointerBuffer pCmds = stack.mallocPointer(1);
            pCmds.put(0, commandBuffer.commandBuffer());
            var submitInfo = VkSubmitInfo.calloc(1, stack)
                    .sType$Default()
                    .pCommandBuffers(pCmds);
            long fenceHandle = fence != null ? fence.getVkFence() : VK_NULL_HANDLE;

            checkVk(vkQueueSubmit(vkQueue, submitInfo, fenceHandle),
                    "submitting commands");
        }
    }
}
