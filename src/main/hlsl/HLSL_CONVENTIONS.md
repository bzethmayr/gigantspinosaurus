# HLSL Conventions for Gigantspinosaurus GPU System

## DO

- **Use `RWStructuredBuffer<T>` (or `StructuredBuffer<T>`) for all GPU data.**
  The Java model only knows about `STORAGE_BUFFER` and `UNIFORM_BUFFER` bindings. Buffers are the only supported resource type.

- **Annotate every buffer binding with `[[vk::binding(slot, set)]]`.**
  Example: `[[vk::binding(0, 0)]] RWStructuredBuffer<uint> buf : register(u0);`
  The slot number must match the `ResourceBinding.slot` declared when creating the Java-side `GpuProgram`.

- **Use `[[vk::push_constant]]` for small metadata (width, height, params).**
  Push constants have zero descriptor overhead. The Java side sets them via `GpuProgramLoan.setPushConstants(ByteBuffer)`.

- **Name the entry point `main`.**
  The build script hardcodes `-E main` for every `.hlsl` file.

- **Target compute shader model `cs_6_0`.**
  The build script passes `-T cs_6_0` to `dxc.exe`, which produces SPIR-V for Vulkan.

- **Use `[numthreads(8, 8, 1)]` as the default thread-group size.**
  Both canonical examples use this value.

- **Perform manual 2D → 1D index conversion when simulating 2D access.**
  Buffers are 1D; compute the flat index: `uint flatIndex = (tid.y * width) + tid.x;`

- **Guard buffer reads/writes with bounds checks.**
  Always validate thread IDs against push-constant dimensions to prevent OOB access.

- **Use `uint3 tid : SV_DispatchThreadID` for the global thread ID in the kernel signature.**
  This is the standard compute-shader semantic for the dispatch ID.

## DON'T

- **Do NOT use `RWTexture2D`, `Texture2D`, or any texture types.**
  The `package-info.java` explicitly states: *"textures are intentionally not supported. We work via buffers instead."* Files using textures (`test.hlsl`, `red.hlsl`) are explicitly marked as invalid examples.

- **Do NOT use `SamplerState` or any sampler objects.**
  Textures and samplers have no equivalent in the buffer-only model.

- **Do NOT assume an image/swapchain extension is available.**
  The system is compute-only; there are no presentable images or framebuffers.

- **Do NOT use `.r`, `.g`, `.b`, `.a` swizzles or write to `float4` outputs.**
  Those are texture operations; use structured buffer writes instead.

- **Do NOT use `register(u#)` without also providing the `[[vk::binding]]` attribute.**
  The `[[vk::binding]]` attribute is required for correct Vulkan SPIR-V mapping.

## Compilation Pipeline

1. Every `.hlsl` file in `src/main/hlsl/` is compiled by `dxc.exe` to SPIR-V.
2. The output `.spv` goes to `build/generated/spv/`.
3. SPIR-V binaries are bundled as resources and loaded at runtime via `GpuProgram.ProgramDesc`.
4. The Java `GpuContext` maps bindings declared in `ProgramDesc` to the slots used in `[[vk::binding(slot, set)]]`.

## Key Java Model Types

| Java type            | HLSL counterpart                         |
|----------------------|------------------------------------------|
| `GpuBuffer`          | `RWStructuredBuffer<T>` / `StructuredBuffer<T>` |
| `GpuProgram`         | Compiled SPIR-V module                   |
| `GpuProgramLoan`     | Per-dispatch binding + push-constants    |
| `ResourceBinding`    | `[[vk::binding(slot, set)]]` attribute   |
