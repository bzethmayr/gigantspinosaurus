package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;

public interface GpuBuffer extends AutoCloseable {
    // opaque; no public ctor, no raw int handle
    long size();
    void upload(long offset, ByteBuffer src);
    void download(long offset, ByteBuffer dst);

    record BufferDesc(
            long sizeBytes,
            BufferUsage usage,
            MemoryHint memoryHint
    ) {}

    enum BufferUsage {
        STORAGE,
        UNIFORM,
        VERTEX,
        INDEX,
        TRANSFER_SRC,
        TRANSFER_DST
    }

    enum MemoryHint {
        CPU_VISIBLE,
        GPU_ONLY
    }
}