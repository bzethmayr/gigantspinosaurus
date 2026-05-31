package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuProgram;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import java.nio.LongBuffer;
import java.util.List;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.CloseableFactory.closeable;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

final class VulkanPipeline implements GpuProgram {
    final VkDevice logicalDevice;
    final long shaderModule;
    final long descriptorSetLayout;
    final long pipelineLayout;
    final long pipeline;
    final long descriptorPool;
    final List<GpuProgram.ResourceBinding> bindings;
    private final ClosingChain close;

    public VulkanPipeline(
            VkDevice logicalDevice,
            long shaderModule,
            long descriptorSetLayout,
            long pipelineLayout,
            long pipeline,
            List<GpuProgram.ResourceBinding> bindings
    ) {
        this.logicalDevice = logicalDevice;
        this.shaderModule = shaderModule;
        this.descriptorSetLayout = descriptorSetLayout;
        this.pipelineLayout = pipelineLayout;
        this.pipeline = pipeline;
        this.bindings = bindings;
        ClosingChain chain = null;
        try {
            this.descriptorPool = createDescriptorPool(bindings);
            chain = new ClosingChain(closeable(descriptorPool, pool -> vkDestroyDescriptorPool(logicalDevice, pool, null)));
            chain = chain.link(closeable(shaderModule, m -> vkDestroyShaderModule(logicalDevice, m, null)));
            chain = chain.link(closeable(descriptorSetLayout, l -> vkDestroyDescriptorSetLayout(logicalDevice, l, null)));
            chain = chain.link(closeable(pipelineLayout, l -> vkDestroyPipelineLayout(logicalDevice, l, null)));
            chain = chain.link(closeable(pipeline, p -> vkDestroyPipeline(logicalDevice, p, null)));
        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        close = chain;
    }

    private long createDescriptorPool(final List<GpuProgram.ResourceBinding> bindings) {
        try (final MemoryStack stack = stackPush()) {
            int storagePerSet = 0;
            int uniformPerSet = 0;
            for (final GpuProgram.ResourceBinding binding : bindings) {
                switch (binding.kind()) {
                    case STORAGE_BUFFER -> storagePerSet++;
                    case UNIFORM_BUFFER -> uniformPerSet++;
                }
            }
            final int maxSets = 16;
            final int poolSizeCount = (storagePerSet > 0 ? 1 : 0) + (uniformPerSet > 0 ? 1 : 0);
            final VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(Math.max(poolSizeCount, 1), stack);
            int sizeIndex = 0;
            if (storagePerSet > 0) {
                poolSizes.get(sizeIndex)
                        .type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                        .descriptorCount(storagePerSet * maxSets);
                sizeIndex++;
            }
            if (uniformPerSet > 0) {
                poolSizes.get(sizeIndex)
                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(uniformPerSet * maxSets);
                sizeIndex++;
            }
            if (storagePerSet == 0 && uniformPerSet == 0) {
                poolSizes.get(0)
                        .type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                        .descriptorCount(maxSets);
                sizeIndex++;
            }
            poolSizes.limit(sizeIndex);

            final VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
                    .maxSets(maxSets)
                    .pPoolSizes(poolSizes);

            final LongBuffer pPool = stack.mallocLong(1);
            checkVk(vkCreateDescriptorPool(logicalDevice, poolInfo, null, pPool),
                    "creating descriptor pool");
            return pPool.get(0);
        }
    }

    @Override
    public void close() throws Exception {
        close.close();
    }
}
