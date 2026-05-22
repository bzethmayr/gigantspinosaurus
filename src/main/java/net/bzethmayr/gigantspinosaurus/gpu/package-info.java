/**
 * Compute-only GPU abstraction.
 * Note that textures are intentionally not supported. We work via buffers instead.
 * Note also that e.g. image and swapchain extensions are therefore unnecessary.
 *
 * <p>The package is built around four core abstractions:
 *
 * <dl>
 *   <dt>{@link net.bzethmayr.gigantspinosaurus.gpu.GpuContext}</dt>
 *   <dd>Factory and lifecycle owner. Creates {@link net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer} and {@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgram}
 *       instances, and mediates program execution via {@link net.bzethmayr.gigantspinosaurus.gpu.GpuContext#withProgram}.</dd>
 *
 *   <dt>{@link net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer}</dt>
 *   <dd>Opaque, typed data buffer. Supports upload/download from the CPU and can be
 *       flagged as CPU-visible or GPU-only, with a usage hint
 *       ({@link net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer.BufferUsage#STORAGE},
 *       {@link net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer.BufferUsage#UNIFORM}, etc.).</dd>
 *
 *   <dt>{@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgram}</dt>
 *   <dd>A compiled shader program described by an entry point, shader stage
 *       {@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgram.ShaderStage#COMPUTE}),
 *       SPIR-V bytecode, and a list of resource bindings (slot + kind). Resource kinds
 *       include
 *       {@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgram.ResourceKind#STORAGE_BUFFER}
 *       and
 *       {@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgram.ResourceKind#UNIFORM_BUFFER}.</dd>
 *
 *   <dt>{@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgramLoan}</dt>
 *   <dd>A short-lived handle obtained inside a {@link net.bzethmayr.gigantspinosaurus.gpu.UsesGpuProgram} callback that
 *       allows binding buffers to slots, setting push constants, and issuing compute
 *       dispatches. This follows an RAII-style loan pattern: the loan is valid only
 *       within the enclosing
 *       {@link net.bzethmayr.gigantspinosaurus.gpu.GpuContext#withProgram} call.</dd>
 * </dl>
 *
 * <p>{@link net.bzethmayr.gigantspinosaurus.gpu.UsesGpuProgram} is a {@link java.lang.FunctionalInterface} extending
 * {@link java.util.function.Consumer}&lt;{@link net.bzethmayr.gigantspinosaurus.gpu.GpuProgramLoan}&gt;, intended for use as a lambda or method reference
 * when submitting work to the GPU.
 *
 * <p>All major types extend {@link java.lang.AutoCloseable} so that resources can be managed
 * with try-with-resources or explicit {@link java.lang.AutoCloseable#close()} calls.
 */
package net.bzethmayr.gigantspinosaurus.gpu;