# Vulkan Implementation Architecture

The `net.bzethmayr.gigantspinosaurus.usage.vk` package provides a Vulkan implementation
of the `gpu` package's abstractions.

## Bootstrap pipeline

1. **InstanceCreation** — enumerates instance layers/extensions and builds
   `VkInstanceCreateInfo`. Supports platform portability on macOS.
2. **PhysicalDeviceSelection** — enumerates all physical devices and scores them
   via pluggable `ToIntFunction<PhysicalDeviceMetadata>` scorers. Negative scores
   act as vetoes (e.g. `noComputeQueue(-100)`).
3. **PhysicalDeviceMetadata** — `AutoCloseable` cache of device properties,
   features, memory properties, extensions, and queue family properties.
4. **LogicalDeviceCreation** — creates a `VkDevice`, filtering requested instance
   extensions down to those actually supported by the device.
5. **QueueSelection** — scores queue families with scorers like `computeQueueOr()`,
   `dedicatedCompute()`, and `countBonus()`.

## Core implementations

- **VulkanRoot** implements `GpuContext` — owns the instance, device, and queue
  lifecycle via a `ClosingChain`. Factory for `VulkanBuffer` and `VulkanPipeline`.
- **VulkanBuffer** implements `GpuBuffer` — wraps `VkBuffer` + `VkDeviceMemory`
  with lazy mapping, coherent/non-coherent upload/download, and atom-aware range
  flushing.
- **VulkanPipeline** implements `GpuProgram` — skeletal, holds shader module,
  descriptor set layout, pipeline layout, and pipeline handles.

## Supporting classes

- **VulkanCommon** — OS detection, name encoding helpers (ASCII/UTF-8),
  `checkVk()` error checking, and `indexOfMaxScorePassing()` selection algorithm.
- **VulkanQueue** — simple record coupling a queue family index with its `VkQueue`.
- **CmdPool** — placeholder for command pool management.

## Design patterns

- **Scoring-based selection** — both physical devices and queue families are
  selected by summing scores from small `ToIntFunction` lambdas. This keeps the
  selection logic composable and avoids rigid if-else chains.
- **ClosingChain** — deterministic, ordered cleanup of Vulkan resources. Each
  link wraps a `close()` action; chains can be extended (`.link()`) or swapped
  (`.swap()`) to handle partial failure during construction.
- **Stateless utilities** — all helper classes (`InstanceCreation`,
  `PhysicalDeviceSelection`, `QueueSelection`, `LogicalDeviceCreation`,
  `VulkanCommon`) have private constructors and expose only static methods.

## Current status

Several components are marked `@NotDone`: `VulkanRoot` (program dispatch
stubbed), `VulkanPipeline` (empty close), `CmdPool` (empty class). See also
the `VulkanBuffer.encodeMemoryHint()` which uses raw integer literals.
