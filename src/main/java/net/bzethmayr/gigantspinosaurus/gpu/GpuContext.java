package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public interface GpuContext extends AutoCloseable {
    String APPLICATION_NAME = "MAR";
    GpuBuffer createBuffer(GpuBuffer.BufferDesc desc);
    GpuProgram createProgram(GpuProgram.ProgramDesc desc);

    void withProgram(GpuProgram program, UsesGpuProgram user);
    void asJob(SpecifiesGpuJob specifier);

    default GpuProgram loadProgram(Class<?> caller, String resourcePath, int numBindings, int pushConstantSize) {
        try (final var stream = caller.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new RuntimeException("Shader not found: " + resourcePath);
            }
            final var spvBytes = stream.readAllBytes();
            final var spvBuffer = ByteBuffer.allocateDirect(spvBytes.length);
            spvBuffer.put(spvBytes);
            spvBuffer.flip();
            final var bindingList = new ArrayList<GpuProgram.ResourceBinding>();
            for (int i = 0; i < numBindings; i++) {
                bindingList.add(new GpuProgram.ResourceBinding(i, GpuProgram.ResourceKind.STORAGE_BUFFER));
            }
            final var desc = new GpuProgram.ProgramDesc("main", GpuProgram.ShaderStage.COMPUTE,
                    spvBuffer, bindingList, pushConstantSize);
            return createProgram(desc);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load program: " + resourcePath, e);
        }
    }

    default ByteBuffer exchangeBuffer(final int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.LITTLE_ENDIAN);
    }
}