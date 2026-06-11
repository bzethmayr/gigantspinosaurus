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

- **VulkanRoot** implements `GpuContext` — owns the instance, device, queue,
  and command pool lifecycle via a `ClosingChain`. `withProgram()` and `asJob()`
  create a `CmdBuffer`, record the `GpuJobSpec`, submit via `Fence`, and wait
  for completion. Factory for `VulkanBuffer` and `VulkanPipeline`.
- **VulkanBuffer** implements `GpuBuffer` — wraps `VkBuffer` + `VkDeviceMemory`
  with lazy mapping, coherent/non-coherent upload/download, and atom-aware range
  flushing.
- **VulkanPipeline** implements `GpuProgram` — manages shader module, descriptor
  set layout, pipeline layout, pipeline handle, and a descriptor pool. Resource
  cleanup is handled via `ClosingChain` (`createDescriptorPool()` allocates a
  `VkDescriptorPool` sized for up to 16 descriptor sets).

## Generic Supporting

*(utilities referenced by the Vulkan package that are broadly reusable outside
Vulkan)*

- **ClosingChain** — deterministic, ordered cleanup; each link wraps a `close()`
  action; chains can be extended (`.link()`) or swapped (`.swap()`) to handle
  partial failure during construction. Defined in
  `net.bzethmayr.gigantspinosaurus.util`.

- **Resettable** — single-method capability interface (`void reset()`) for
  resources that can be returned to their initial state. Implemented by
  `CmdPool` and `Fence`. Defined in
  `net.bzethmayr.gigantspinosaurus.capabilities`.

- **VulkanCommon** — OS detection (`getOS()`), predicate composition (`optionalAny()`, `filteredList()`),
  name encoding helpers (`asciiNamesFrom()`, `utf8NamesFlippedFrom()`),
  `checkVk()` error checking, and `indexOfMaxScorePassing()` selection
  algorithm (generic `int[]` argmax with negative-score veto).

## Specific Supporting

*(Vulkan-specific utility classes shared across multiple core implementations)*

- **VulkanQueue** — record coupling a queue family index with its `VkQueue`.
  Exposes `submit(VkCommandBufferSubmitInfo, Fence)` and `waitIdle()`.

- **CmdPool** — manages a `VkCommandPool` lifecycle; implements `AutoCloseable`
  and `Resettable`. Supports `RESET_COMMAND_BUFFER_BIT` (configurable at
  construction). Created during `VulkanRoot` construction for the selected queue
  family.

- **CmdBuffer** — allocates and records command buffers from a `CmdPool`.
  `beginRecording()`/`endRecording()` frame CPU-side recording;
  `acceptSpec(GpuJobSpec)` iterates job stages, binding pipelines, allocating
  descriptor sets, handling barriers, and dispatching compute work.
  `submitAndWait(VulkanQueue)` submits via a `Fence` and waits. Inner
  `VulkanProgramLoan` record implements `GpuProgramLoan` for per-dispatch
  descriptor/buffer binding and push constants.

- **Fence** — wraps a `VkFence`; implements `AutoCloseable` and `Resettable`.
  Constructed signalled or unsignalled. `fenceWait()` blocks until the GPU
  signals. Used by `CmdBuffer.submitAndWait()`.

## Design patterns

- **Scoring-based selection** — both physical devices and queue families are
  selected by summing scores from small `ToIntFunction` lambdas. This keeps the
  selection logic composable and avoids rigid if-else chains.
- **Stateless utilities** — all helper classes (`InstanceCreation`,
  `PhysicalDeviceSelection`, `QueueSelection`, `LogicalDeviceCreation`,
  `VulkanCommon`) have private constructors and expose only static methods.
- **Resettable capability** — `CmdPool` and `Fence` both implement
  `net.bzethmayr.gigantspinosaurus.capabilities.Resettable`, allowing command
  pools and fences to be reused across multiple submissions.

## Status

Do not PIT — mutation-testing Vulkan tests will crash your machine.

## Resource lifecycle

GPU resources exposed to callers (`VulkanBuffer`, `VulkanPipeline`,
`CmdBuffer`, `Fence`) implement `AutoCloseable`.  You are only responsible
for closing objects you create directly — every `createBuffer` / `createProgram`
call returns a resource you own.  Resources owned by `VulkanRoot` (instance,
device, queue, command pool) are managed by its `ClosingChain` and closed
automatically when the root is closed.  Transient resources such as the
`Fence` inside `submitAndWait` are scoped by try-with-resources.
