package net.bzethmayr.gigantspinosaurus.usage.vk;

import net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer;
import net.zethmayr.fungu.core.declarations.NotDone;

import java.nio.ByteBuffer;

@NotDone
public class VkBuffer implements GpuBuffer {
    final long buffer;
    final long memory;
    final long size;

    public VkBuffer(long buffer, long memory, long size) {
        this.buffer = buffer;
        this.memory = memory;
        this.size = size;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void upload(long offset, ByteBuffer src) {

    }

    @Override
    public void download(long offset, ByteBuffer dst) {

    }
}
