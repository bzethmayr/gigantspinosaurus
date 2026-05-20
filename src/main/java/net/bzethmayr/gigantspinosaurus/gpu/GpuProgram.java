package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;
import java.util.List;

public interface GpuProgram extends AutoCloseable {
    record ProgramDesc(
            String entryPoint,
            ShaderStage stage,
            ByteBuffer spirvOrBinary,
            List<ResourceBinding> bindings
    ) {}

    enum ShaderStage { COMPUTE }
    enum ResourceKind { STORAGE_BUFFER, UNIFORM_BUFFER }

    record ResourceBinding(
            int slot,
            ResourceKind kind
    ) {}
}
