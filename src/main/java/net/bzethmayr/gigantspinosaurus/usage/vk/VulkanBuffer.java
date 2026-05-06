package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.bzethmayr.gigantspinosaurus.util.ClosingChain;
import net.zethmayr.fungu.core.declarations.NotDone;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Optional;

import static net.bzethmayr.gigantspinosaurus.usage.vk.VulkanCommon.checkVk;
import static net.zethmayr.fungu.CloseableFactory.closeable;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.*;

@NotDone
class VulkanBuffer implements GpuBuffer {
    private final long requestedSize;
    private final long buffer;
    private final long memory;
    private long mappedMemory;
    private final PointerBuffer impl;
    private final long allocatedSize;
    private final ClosingChain close;

    public VulkanBuffer(
            final MemoryStack stack,
            final PhysicalDeviceMetadata metadata,
            final VkDevice logicalDevice,
            final BufferDesc desc
            ) {
        requestedSize = desc.sizeBytes();
        mappedMemory = NULL;
        ClosingChain chain = null;
        try {
            final VkBufferCreateInfo bufSpec = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(requestedSize)
                    .usage(encodeUsage(desc.usage()))
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            final LongBuffer xfr = stack.mallocLong(1);
            checkVk(vkCreateBuffer(logicalDevice, bufSpec, null, xfr),
                    "creating buffer");
            buffer = xfr.get(0);
            chain = new ClosingChain(closeable(buffer, b -> vkDestroyBuffer(logicalDevice, b, null)));

            final VkMemoryRequirements memReq = VkMemoryRequirements.calloc(stack);
            vkGetBufferMemoryRequirements(logicalDevice, buffer, memReq);
            final VkMemoryAllocateInfo memSpec = VkMemoryAllocateInfo.calloc(stack)
                    .sType$Default()
                    .allocationSize(memReq.size())
                    .memoryTypeIndex(findMemoryType(
                            metadata.memory(), memReq.memoryTypeBits(), encodeMemoryHint(desc.memoryHint())));
            checkVk(vkAllocateMemory(logicalDevice, memSpec, null, xfr),
                    "allocating buffer memory");
            memory = xfr.get(0);
            chain = chain.swap(closeable(memory, m -> vkFreeMemory(logicalDevice, m, null)));
            allocatedSize = memSpec.allocationSize();
            impl = MemoryUtil.memAllocPointer(1);
            chain = chain.link(closeable(impl, MemoryUtil::memFree));
            checkVk(vkBindBufferMemory(logicalDevice, buffer, memory, 0),
                    "binding buffer memory");
        } catch (final Exception e) {
            Optional.ofNullable(chain).ifPresent(ClosingChain::close);
            throw new RuntimeException(e);
        }
        close = chain;
    }

    static int findMemoryType(
            final VkPhysicalDeviceMemoryProperties memory,
            int typeBits,
            final int usageMask
    ) {
        int found = Integer.MIN_VALUE;
        final VkMemoryType.Buffer memoryTypes = memory.memoryTypes();
        for (int i = 0; i < VK_MAX_MEMORY_TYPES; i++) {
            if ((typeBits & 1) == 1 && (memoryTypes.get(i).propertyFlags() & usageMask) == usageMask) {
                found = i;
                break;
            }
            typeBits >>= 1;
        }
        if (found < 0) {
            throw new RuntimeException("Failed to find memoryType");
        }
        return found;
    }

    static int encodeUsage(final BufferUsage usage) {
        return switch (usage) {
            case STORAGE -> VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
            case UNIFORM -> VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
            case VERTEX -> VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
            case INDEX -> VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
            case TRANSFER_SRC -> VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
            case TRANSFER_DST -> VK_BUFFER_USAGE_TRANSFER_DST_BIT;
        };
    }

    static int encodeMemoryHint(final MemoryHint hint) {
        return switch (hint) {
            case CPU_VISIBLE -> 2;
            case GPU_ONLY -> 1;
        };
    }

    @Override
    public void close() {
        close.close();
    }

    @Override
    public long size() {
        return requestedSize;
    }

    @Override
    public void upload(long offset, ByteBuffer src) {

    }

    @Override
    public void download(long offset, ByteBuffer dst) {

    }
}
