package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.zethmayr.fungu.core.declarations.NotDone;

@NotDone
public final class VkComputePipeline implements AutoCloseable {
    final long shaderModule;
    final long descriptorSetLayout;
    final long pipelineLayout;
    final long pipeline;

    public VkComputePipeline(long shaderModule, long descriptorSetLayout, long pipelineLayout, long pipeline) {
        this.shaderModule = shaderModule;
        this.descriptorSetLayout = descriptorSetLayout;
        this.pipelineLayout = pipelineLayout;
        this.pipeline = pipeline;
    }

    @Override
    public void close() throws Exception {

    }
}
