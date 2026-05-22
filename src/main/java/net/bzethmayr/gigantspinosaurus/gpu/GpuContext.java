package net.bzethmayr.gigantspinosaurus.gpu;

public interface GpuContext extends AutoCloseable {
    String APPLICATION_NAME = "MAR";
    GpuBuffer createBuffer(GpuBuffer.BufferDesc desc);
    GpuProgram createProgram(GpuProgram.ProgramDesc desc);

    void withProgram(GpuProgram program, UsesGpuProgram user);
    void asJob(SpecifiesGpuJob specifier);
}