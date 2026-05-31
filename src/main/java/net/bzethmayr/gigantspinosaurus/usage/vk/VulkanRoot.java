package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import net.zethmayr.fungu.core.declarations.ReuseResults;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.gpu.GpuJobSpec.emptySpec;
import static net.bzethmayr.gigantspinosaurus.usage.vk.QueueSelection.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.QueueSelection.selectQueue;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.OSType.MACOS;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.InstanceCreation.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.LogicalDeviceCreation.configureLogicalDevice;
import static net.bzethmayr.gigantspinosaurus.usage.vk.PhysicalDeviceSelection.*;
import static net.zethmayr.fungu.CloseableFactory.closeable;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public final class VulkanRoot implements GpuContext {
    static final String ENGINE_NAME = "vermillion";
    private final ClosingChain closeChain;
    private final VkInstance instance;
    private final VkPhysicalDevice physicalDevice;
    private final PhysicalDeviceMetadata physicalMetadata;
    private final VkDevice logicalDevice;
    private final VulkanQueue queue;
    private final int queueFamily;
    private final CmdPool cmdPool;

    @ReuseResults
    public VulkanRoot() {
        ClosingChain chain = null;
        try (MemoryStack stack = stackPush()) {
            VkApplicationInfo appInfo = appInfo(stack, APPLICATION_NAME, ENGINE_NAME);
            final List<String> layerNames = instanceLayerNames(stack,
                    VALIDATION_LAYER::equals);
            final List<String> extensionNames = instanceExtensionNames(stack,
                    s -> getOS() == MACOS && s.equals(PORTABILITY_EXTENSION));
            VkInstanceCreateInfo instanceInfo = instanceCreateInfo(stack, appInfo, layerNames, extensionNames);
            PointerBuffer instanceBuf = stack.mallocPointer(1);
            checkVk(vkCreateInstance(instanceInfo, null, instanceBuf), "instance creation");
            instance = new VkInstance(instanceBuf.get(0), instanceInfo);
            chain = new ClosingChain(
                    closeable(instance, i -> vkDestroyInstance(i, null)));

            physicalDevice = selectPhysicalDevice(stack, instance,
                    noComputeQueue(-100), discreteBonus(50));
            physicalMetadata = new PhysicalDeviceMetadata(stack, physicalDevice);
            chain = chain.link(physicalMetadata);
            logicalDevice = configureLogicalDevice(stack, physicalDevice, physicalMetadata, extensionNames);
            chain = chain.link(
                    closeable(logicalDevice, d -> vkDestroyDevice(d, null)));

            queue = selectQueue(stack, physicalMetadata, logicalDevice, 0,
                    computeQueueOr(-100), dedicatedCompute(50), countBonus());
            queueFamily = queue.familyIndex();
            cmdPool = new CmdPool(stack, logicalDevice, queueFamily, true);
            chain = chain.link(cmdPool);

        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        closeChain = chain;
    }

    @Override
    public void close() {
        closeChain.close();
    }

    @Override
    public GpuBuffer createBuffer(GpuBuffer.BufferDesc desc) {
        try (final MemoryStack stack = stackPush()) {
            return new VulkanBuffer(stack, physicalMetadata, logicalDevice, desc);
        }
    }

    @Override
    public GpuProgram createProgram(GpuProgram.ProgramDesc desc) {
        try (final MemoryStack stack = stackPush()) {
            var moduleCreateInfo = VkShaderModuleCreateInfo.calloc(stack)
                    .sType$Default()
                    .pCode(desc.spirvOrBinary());
            LongBuffer pModule = stack.mallocLong(1);
            checkVk(vkCreateShaderModule(logicalDevice, moduleCreateInfo, null, pModule),
                    "creating shader module");
            long shaderModule = pModule.get(0);

            var bindings = desc.bindings();
            var layoutBindings = VkDescriptorSetLayoutBinding.calloc(bindings.size(), stack);
            for (int i = 0; i < bindings.size(); i++) {
                var binding = bindings.get(i);
                int descriptorType = switch (binding.kind()) {
                    case STORAGE_BUFFER -> VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
                    case UNIFORM_BUFFER -> VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
                };
                layoutBindings.get(i)
                        .binding(binding.slot())
                        .descriptorType(descriptorType)
                        .descriptorCount(1)
                        .stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            }
            var setLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .pBindings(layoutBindings);
            LongBuffer pSetLayout = stack.mallocLong(1);
            checkVk(vkCreateDescriptorSetLayout(logicalDevice, setLayoutCreateInfo, null, pSetLayout),
                    "creating descriptor set layout");
            long descriptorSetLayout = pSetLayout.get(0);

            int pushConstantSize = desc.pushConstantSize();
            VkPushConstantRange.Buffer pushConstantRange = null;
            if (pushConstantSize > 0) {
                pushConstantRange = VkPushConstantRange.calloc(1, stack)
                        .stageFlags(VK_SHADER_STAGE_COMPUTE_BIT)
                        .offset(0)
                        .size(pushConstantSize);
            }
            var pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                    .sType$Default()
                    .pSetLayouts(stack.longs(descriptorSetLayout));
            if (pushConstantRange != null) {
                pipelineLayoutCreateInfo.pPushConstantRanges(pushConstantRange);
            }
            LongBuffer pPipelineLayout = stack.mallocLong(1);
            checkVk(vkCreatePipelineLayout(logicalDevice, pipelineLayoutCreateInfo, null, pPipelineLayout),
                    "creating pipeline layout");
            long pipelineLayout = pPipelineLayout.get(0);

            var computePipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1, stack)
                    .sType$Default()
                    .layout(pipelineLayout);
            computePipelineCreateInfo.stage()
                    .sType$Default()
                    .stage(VK_SHADER_STAGE_COMPUTE_BIT)
                    .module(shaderModule)
                    .pName(stack.ASCII(desc.entryPoint()));
            LongBuffer pPipeline = stack.mallocLong(1);
            checkVk(vkCreateComputePipelines(logicalDevice, VK_NULL_HANDLE,
                    computePipelineCreateInfo, null, pPipeline),
                    "creating compute pipeline");
            long pipeline = pPipeline.get(0);

            return new VulkanPipeline(logicalDevice, shaderModule, descriptorSetLayout,
                    pipelineLayout, pipeline, bindings);
        }
    }

    @Override
    public void withProgram(GpuProgram program, UsesGpuProgram user) {
        asJob(s -> s.stage(program, user));
    }

    @Override
    public void asJob(final SpecifiesGpuJob specifier) {
        final GpuJobSpec spec = emptySpec();
        specifier.accept(spec);
        try (final CmdBuffer primary = new CmdBuffer(logicalDevice, cmdPool, true, true)) {
            primary.beginRecording();
            primary.acceptSpec(spec);
            primary.endRecording();
            primary.submitAndWait(queue);
        }
    }

    VkInstance instance() {
        return instance;
    }

    VkPhysicalDevice physicalDevice() {
        return physicalDevice;
    }

    PhysicalDeviceMetadata physicalMetadata() {
        return physicalMetadata;
    }

    int queueFamily() {
        return queueFamily;
    }
}
