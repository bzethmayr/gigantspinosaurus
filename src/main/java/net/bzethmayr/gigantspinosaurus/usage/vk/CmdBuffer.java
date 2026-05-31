package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.bzethmayr.gigantspinosaurus.gpu.GpuJobSpec;
import net.bzethmayr.gigantspinosaurus.gpu.GpuProgram;
import net.bzethmayr.gigantspinosaurus.gpu.GpuProgramLoan;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

class CmdBuffer implements AutoCloseable {
    private final VkDevice logicalDevice;
    private final long boundPool;
    private final boolean oneTimeSubmit;
    private final boolean primary;
    private final VkCommandBuffer vkCommandBuffer;
    private volatile boolean recording;

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
            recording = true;
        }
    }

    public void acceptSpec(final GpuJobSpec spec) {
        if (!recording) throw becauseImpossible("Not recording");
        try (var stack = stackPush()) {
            var parts = spec.parts().toList();
            for (var part : parts) {
                switch (part) {
                    case GpuJobSpec.Stage s -> {
                        var pipeline = (VulkanPipeline) s.program();
                        vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline.pipeline);

                        var allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                                .sType$Default()
                                .descriptorPool(pipeline.descriptorPool)
                                .pSetLayouts(stack.longs(pipeline.descriptorSetLayout));
                        LongBuffer pSet = stack.mallocLong(1);
                        checkVk(vkAllocateDescriptorSets(logicalDevice, allocInfo, pSet),
                                "allocating descriptor set");
                        long descriptorSet = pSet.get(0);

                        var loan = new VulkanProgramLoan(
                                vkCommandBuffer, logicalDevice, pipeline, descriptorSet, stack);
                        s.user().accept(loan);
                    }
                    case GpuJobSpec.Barrier b -> {
                        vkCmdPipelineBarrier(vkCommandBuffer,
                                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                                0, null, null, null);
                    }
                }
            }
        }
    }

    public void endRecording() {
        checkVk(vkEndCommandBuffer(vkCommandBuffer), "ending recording");
    }

    public void close() {
        vkFreeCommandBuffers(logicalDevice, boundPool,
                vkCommandBuffer);
    }

    public VkCommandBuffer getVkCommandBuffer() {
        return vkCommandBuffer;
    }

    public void submitAndWait(final VulkanQueue queue) {
        try (Fence fence = new Fence(logicalDevice, false)) {
            try (var stack = stackPush()) {
                var cmds = VkCommandBufferSubmitInfo.calloc(1, stack)
                        .sType$Default()
                        .commandBuffer(vkCommandBuffer);
                queue.submit(cmds, fence);
            }
            fence.fenceWait();
        }
    }

    private record VulkanProgramLoan(
            VkCommandBuffer cmdBuf,
            VkDevice device,
            VulkanPipeline pipeline,
            long descriptorSet,
            MemoryStack stack
    ) implements GpuProgramLoan {
        @Override
        public GpuProgramLoan bindBuffer(int slot, GpuBuffer buffer) {
            long vkBuffer = ((VulkanBuffer) buffer).getVkBuffer();
            int descriptorType = descriptorTypeForSlot(slot);
            var bufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                    .buffer(vkBuffer)
                    .offset(0)
                    .range(VK_WHOLE_SIZE);
            var write = VkWriteDescriptorSet.calloc(1, stack)
                    .sType$Default()
                    .dstSet(descriptorSet)
                    .dstBinding(slot)
                    .descriptorCount(1)
                    .descriptorType(descriptorType)
                    .pBufferInfo(bufferInfo);
            vkUpdateDescriptorSets(device, write, null);
            return this;
        }

        @Override
        public GpuProgramLoan setScalars(ByteBuffer data) {
            vkCmdPushConstants(cmdBuf, pipeline.pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0, data);
            return this;
        }

        @Override
        public void dispatch(int xGroups, int yGroups, int zGroups) {
            var descriptorSets = stack.longs(descriptorSet);
            vkCmdBindDescriptorSets(cmdBuf, VK_PIPELINE_BIND_POINT_COMPUTE,
                    pipeline.pipelineLayout, 0, descriptorSets, (IntBuffer) null);
            vkCmdDispatch(cmdBuf, xGroups, yGroups, zGroups);
        }

        private int descriptorTypeForSlot(int slot) {
            for (var b : pipeline.bindings) {
                if (b.slot() == slot) {
                    return switch (b.kind()) {
                        case STORAGE_BUFFER -> VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
                        case UNIFORM_BUFFER -> VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
                    };
                }
            }
            throw becauseImpossible("No binding for slot %s", slot);
        }
    }
}
