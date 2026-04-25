package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.*;
import net.zethmayr.fungu.core.declarations.NotDone;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_API_VERSION_1_0;

@NotDone
public final class VkContext implements GpuContext {
    static final String ENGINE_NAME = "vermillion";
//    final VkInstance instance;
//    final VkPhysicalDevice physicalDevice;
//    final VkDevice logicalDevice;
//    final VkQueue queue;
//    final int queueFamily;
//    final long commandPool;

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
