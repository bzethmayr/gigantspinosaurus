package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;
import java.util.List;

public interface GpuProgram extends AutoCloseable {
    record ProgramDesc(
            String entryPoint,
            ShaderStage stage,
            ByteBuffer spirvOrBinary,
            List<ResourceBinding> bindings,
            int pushConstantSize
    ) {
        public ProgramDesc(String entryPoint, ShaderStage stage, ByteBuffer spirvOrBinary, List<ResourceBinding> bindings) {
            this(entryPoint, stage, spirvOrBinary, bindings, 0);
        }
    }

    enum ShaderStage { COMPUTE }
    enum ResourceKind { STORAGE_BUFFER, UNIFORM_BUFFER }

    record ResourceBinding(
            int slot,
            ResourceKind kind
    ) {}
}
