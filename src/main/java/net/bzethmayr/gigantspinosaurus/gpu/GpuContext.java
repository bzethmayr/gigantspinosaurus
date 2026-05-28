package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface GpuContext extends AutoCloseable {
    String APPLICATION_NAME = "MAR";
    GpuBuffer createBuffer(GpuBuffer.BufferDesc desc);
    GpuProgram createProgram(GpuProgram.ProgramDesc desc);

    void withProgram(GpuProgram program, UsesGpuProgram user);
    void asJob(SpecifiesGpuJob specifier);

    default ByteBuffer exchangeBuffer(final int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
    }
}