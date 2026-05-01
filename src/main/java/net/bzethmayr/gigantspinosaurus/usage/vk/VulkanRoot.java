package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.zethmayr.fungu.core.declarations.NotDone;
import net.zethmayr.fungu.core.declarations.ReuseResults;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VkCommon.OSType.MACOS;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VkCommon.*;
import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanInstanceCreation.*;
import static net.zethmayr.fungu.PredicateFactory.anyOf;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR;
import static org.lwjgl.vulkan.VK10.*;

@NotDone
public final class VulkanRoot implements GpuContext {
    static final String ENGINE_NAME = "vermillion";
    private final VkInstance instance;
//    final VkPhysicalDevice physicalDevice;
//    final VkDevice logicalDevice;
//    final VkQueue queue;
//    final int queueFamily;
//    final long commandPool;

    @ReuseResults
    public VulkanRoot() {
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
        }
    }

    @Override
    public void close() {
        vkDestroyInstance(instance, null);
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

    void withInstance(final Consumer<VkInstance> usesInstance) {
        usesInstance.accept(instance);
    }

    <T> T fromInstance(final Function<VkInstance, T> usesInstance) {
        return usesInstance.apply(instance);
    }
}
