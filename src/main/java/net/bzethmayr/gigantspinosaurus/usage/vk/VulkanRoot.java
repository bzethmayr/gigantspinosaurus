package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import net.zethmayr.fungu.core.declarations.NotDone;
import net.zethmayr.fungu.core.declarations.ReuseResults;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.OSType.MACOS;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanInstanceCreation.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanPhysicalDeviceSelection.*;
import static net.zethmayr.fungu.CloseableFactory.closeable;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

@NotDone
public final class VulkanRoot implements GpuContext {
    static final String ENGINE_NAME = "vermillion";
    private final ClosingChain closeChain;
    private final VkInstance instance;
    private final VkPhysicalDevice physicalDevice;
    private final PhysicalDeviceMetadata physicalMetadata;
//    final VkDevice logicalDevice;
//    final VkQueue queue;
//    final int queueFamily;
//    final long commandPool;

    @ReuseResults
    public VulkanRoot() {
        ClosingChain chain = null;
        try (MemoryStack stack = stackPush()) {
            VkApplicationInfo appInfo = appInfo(stack, APPLICATION_NAME, ENGINE_NAME);
            final List<String> layerNames = allLayerNames(stack,
                    VALIDATION_LAYER::equals);
            final List<String> extensionNames = allExtensionNames(stack,
                    s -> getOS() == MACOS && s.equals(PORTABILITY_EXTENSION));
            VkInstanceCreateInfo instanceInfo = instanceCreateInfo(stack, appInfo, layerNames, extensionNames);
            PointerBuffer instanceBuf = stack.mallocPointer(1);
            checkVk(vkCreateInstance(instanceInfo, null, instanceBuf), "instance creation");
            instance = new VkInstance(instanceBuf.get(0), instanceInfo);
            chain = new ClosingChain(
                    closeable(instance, i -> vkDestroyInstance(i, null))
            );
            physicalDevice = selectPhysicalDevice(stack, instance,
                    noComputeQueue(-100),
                    discreteBonus(50)
            );
            physicalMetadata = new PhysicalDeviceMetadata(stack, physicalDevice);
            chain = chain.link(physicalMetadata);
        }
        closeChain = chain;
    }

    @Override
    public void close() {
        closeChain.close();
    }

    @Override
    public GpuBuffer createBuffer(GpuBuffer.BufferDesc desc) {
        return null;
    }

    @Override
    public GpuTexture createTexture(GpuTexture.TextureDesc desc) {
        return null;
    }

    @Override
    public GpuProgram createProgram(GpuProgram.ProgramDesc desc) {
        return null;
    }

    @Override
    public void withProgram(GpuProgram program, UsesGpuProgram user) {

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
}
