package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.zethmayr.fungu.core.declarations.NotDone;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

class CmdBuffer implements AutoCloseable {
    private final VkDevice logicalDevice;
    private final long boundPool;
    private final boolean oneTimeSubmit;
    private final boolean primary;
    private final VkCommandBuffer vkCommandBuffer;

    public CmdBuffer(final VkDevice logicalDevice, CmdPool cmdPool, boolean primary, boolean oneTimeSubmit) {
        this.logicalDevice = logicalDevice;
        boundPool = cmdPool.getPool();
        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;

        try (final MemoryStack stack = stackPush()) {
            var cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType$Default()
                    .commandPool(boundPool)
                    .level(primary ? VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                    .commandBufferCount(1);
            PointerBuffer pb = stack.mallocPointer(1);
            checkVk(vkAllocateCommandBuffers(logicalDevice, cmdBufAllocateInfo, pb),
                    "allocating command buffer");

            vkCommandBuffer = new VkCommandBuffer(pb.get(0), logicalDevice);
        }
    }

    public void beginRecording() {
        try (var stack = stackPush()) {
            var cmdBufInfo = VkCommandBufferBeginInfo.calloc(stack).sType$Default();
            if (oneTimeSubmit) {
                cmdBufInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            }
            if (!primary) {
                var vkInheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
                        .sType$Default();
                cmdBufInfo.pInheritanceInfo(vkInheritanceInfo);
            }
            checkVk(vkBeginCommandBuffer(vkCommandBuffer, cmdBufInfo), "starting recording");
        }
    }

    public void close() {
        vkFreeCommandBuffers(logicalDevice, boundPool,
                vkCommandBuffer);
    }

    public void endRecording() {
        checkVk(vkEndCommandBuffer(vkCommandBuffer), "ending recording");
    }

    public VkCommandBuffer getVkCommandBuffer() {
        return vkCommandBuffer;
    }

    @NotDone
    public void submitAndWait(final VulkanQueue queue) {
        try (Fence fence = new Fence(logicalDevice, true)) {
            fence.reset();
            try (var stack = stackPush()) {
                var cmds = VkCommandBufferSubmitInfo.calloc(1, stack)
                        .sType$Default()
                        .commandBuffer(vkCommandBuffer);

                //queue.vkQueue()(cmds, null, null, fence);
            }
            fence.fenceWait();
        }
    }
}
