package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import net.zethmayr.fungu.core.declarations.NotDone;
import net.zethmayr.fungu.core.declarations.ReuseResults;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

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

@NotDone
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
        return null;
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
