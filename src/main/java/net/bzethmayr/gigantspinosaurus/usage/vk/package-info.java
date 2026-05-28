/**
 * Vulkan implementation of the {@link net.bzethmayr.gigantspinosaurus.gpu GPU abstraction}.
 *
 * <p>Entry point: {@link net.bzethmayr.gigantspinosaurus.usage.vk.VulkanRoot} implements
 * {@link net.bzethmayr.gigantspinosaurus.gpu.GpuContext}. Construct it with {@code new VulkanRoot()}
 * — this performs device selection and queue setup. Use {@code createBuffer()} and
 * {@code createProgram()} to allocate resources, then {@code withProgram()} to dispatch
 * compute work.
 *
 * <p>Call {@code close()} or use try-with-resources to tear down all Vulkan state
 * deterministically via {@link net.bzethmayr.gigantspinosaurus.util.ClosingChain}.
 *
 * <p>Use {@code asJob()} with a {@link net.bzethmayr.gigantspinosaurus.gpu.SpecifiesGpuJob}
 * to compose multi-stage pipelines via {@link net.bzethmayr.gigantspinosaurus.gpu.GpuJobSpec}.
 *
 * @see net.bzethmayr.gigantspinosaurus.gpu.GpuContext
 * @see net.bzethmayr.gigantspinosaurus.gpu.GpuBuffer
 * @see net.bzethmayr.gigantspinosaurus.gpu.GpuProgram
 */
package net.bzethmayr.gigantspinosaurus.usage.vk;