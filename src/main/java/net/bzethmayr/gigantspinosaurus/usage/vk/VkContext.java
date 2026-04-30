package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.zethmayr.fungu.core.declarations.NotDone;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkLayerProperties;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_API_VERSION_1_0;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceLayerProperties;

@NotDone
public final class VkContext implements GpuContext {
    static final String ENGINE_NAME = "vermillion";
//    final VkInstance instance;
//    final VkPhysicalDevice physicalDevice;
//    final VkDevice logicalDevice;
//    final VkQueue queue;
//    final int queueFamily;
//    final long commandPool;

    public VkContext() {
        try (MemoryStack stack = stackPush()) {
            VkApplicationInfo appInfo = appInfo(stack);
        }
    }

    static VkApplicationInfo appInfo(final MemoryStack stack) {
        final ByteBuffer appName = stack.UTF8(APPLICATION_NAME);
        final ByteBuffer engineName = stack.UTF8(ENGINE_NAME);
        return VkApplicationInfo.calloc(stack)
                .sType$Default()
                .pApplicationName(appName)
                .applicationVersion(1)
                .pEngineName(engineName)
                .engineVersion(1)
                .apiVersion(VK_API_VERSION_1_0);
    }

    static List<String> allLayerNames(final MemoryStack stack) {
        final IntBuffer layerBuf = stack.callocInt(1);
        vkEnumerateInstanceLayerProperties(layerBuf, null);
        final int numLayers = layerBuf.get(0);
        final VkLayerProperties.Buffer propsBuf = VkLayerProperties.calloc(numLayers, stack);
        vkEnumerateInstanceLayerProperties(layerBuf, propsBuf);
        final List<String> layers = new ArrayList<>(numLayers);
        for (int i = 0; i < numLayers; i++) {
            final VkLayerProperties props = propsBuf.get(i);
            layers.add(props.layerNameString());
        }
        return layers;
    }

    @Override
    public void close() throws Exception {

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
}
