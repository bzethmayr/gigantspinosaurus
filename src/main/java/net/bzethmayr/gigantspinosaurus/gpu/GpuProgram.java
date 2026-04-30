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

    enum ShaderStage { COMPUTE, VERTEX, FRAGMENT }
    enum ResourceKind { STORAGE_BUFFER, UNIFORM_BUFFER, SAMPLED_TEXTURE, STORAGE_TEXTURE }

    record ResourceBinding(
            int slot,
            ResourceKind kind
    ) {}
}
