# Current Scope Summary

## What gigantspinosaurus IS

A **MAR (Minimal Attestation Record) engine** — byte-level serialization, chaining,
creation, verification, and GPU-accelerated media reduction for cryptographically signed
point-of-fact attestations.  One component of a larger evidence-provenance system.

---

## Implemented (in library)

### Core data model
- `MinimalAttestationRecord`, `ExposesMar`, `MutableMar`, `MarSignature` — MAR frame type, mutable builder, canonical attribute contract
- `ExposesMedia`, `Media` — 4-slot reduction-step pipeline + BLAKE3 hash
- `ExposesPosition`, `Geoposition` — lat/lon/elevation/north
- `ExposesOrientation`, `Orientation`, `ExposesQuaternion`, `QuaternionHelper` — quaternion orientation
- `ExposesFraming`, `Framing`, enums (`North`, `Face`, `Handedness`, `Vertical`) — camera framing conventions
- `ExposesSignature`, `Signatory`, `Signs`, `VerifiesSignature`, `ExposesSigningKeys`, `CreatesSignature` — Ed25519 signing interfaces
- `ExposesUtcDoubleSeconds` — time source
- `GeneratesNonce`, `DefaultNonceFactory` — nonce generation
- `ReductionStep`, `ReductionIds` — media reduction descriptors (DWT/Sobel cell layout)

### Canonical serialization
- `HasCanonicalAttributes`, `HasMappedAttributes`, `BoundAttributes`, `AttributeValuations`, `DecoderHelper`, `Versioned` — `{key:value,}` map format, recursive decode

### MAR pipeline
- `MarCreation` — intent frame (index -1) + `intentToRecord()` closure for chained media frames
- `MarVerification` — rederives hash chain and verifies Ed25519 signature
- `MarDecoding` — full recursive canonical decode of MAR and all sub-records

### GPU compute (video reduction)
- `VideoPipeline` — 5-stage Vulkan compute pipeline: YCbCr→downsample→DWT→Sobel→cell_packing
- `VideoReduction`, `SpatialReduction` — wrappers implementing `ReducesMedia`
- `src/main/hlsl/{ycbcr_reduction,downsample,dwt_ll,sobel_feature,cell_packing}.hlsl` — HLSL source shaders (buffer-only, integer math)
- `build/resources/main/spv/{ycbcr_reduction,downsample,dwt_ll,sobel_feature,cell_packing}.spv` — compiled SPIR-V binaries
- `HLSL_CONVENTIONS.md` — documented buffer-only model
- `red.hlsl`, `test.hlsl` (invalid texture examples), `testu.hlsl` (valid buffer validation)

### GPU abstraction
- `GpuContext`, `GpuBuffer`, `GpuProgram`, `GpuProgramLoan`, `GpuJobSpec`, `SpecifiesGpuJob`, `UsesGpuProgram`

### Vulkan backend
- `VulkanRoot` (GpuContext impl), `VulkanBuffer`, `VulkanPipeline`, `VulkanQueue`, `CmdBuffer`, `CmdPool`, `Fence`
- `InstanceCreation`, `PhysicalDeviceSelection`, `PhysicalDeviceMetadata`, `LogicalDeviceCreation`, `QueueSelection`
- `VulkanCommon` utilities

### Default implementations
- `BindsEnvironment.desktopEnvironment()` — wires `SipMarHasher` + `Blake3MediaHasher` + `SignsForJava15` + desktop sensor stubs
- `BindsConstructors.defaultConstructors()` — wires all model constructors
- `BindsMediaPipeline` — reducer + combiner + marker
- `SipMarHasher` — SipHash 4-8 via `io.whitfin:siphash`
- `Blake3MediaHasher` — BLAKE3 via `io.github.rctcwyvrn:blake3`
- `SignsForJava15` — Ed25519 via `java.security.Signature`

### Near-real-time video marring
- `VideoMarring` — dual-thread state machine (GRAB_FRAME→CALCULATE_MARK→APPLY_MARK→WAIT_EMPTY)

### Mark embedding (stub level)
- `MarkEmbedder` — trivial 600-byte XOR copy into target buffer (placeholder)

### Desktop sensor stubs
- `DesktopPosition` — returns zeros for all fields
- `DesktopOrientation` — fixed quaternion (1, 0, 1, 0); `withFraming` returns null

### Capabilities
- `Resettable` — reset-to-initial-state contract

### Utilities
- `ClosingChain` — deterministic ordered cleanup
- `CollectionHelper` — sequenced collection factories

### Tests
- `MarCreationTest`, `MarVerificationTest`, `MinimalAttestationRecordTest`
- `HasMappedAttributesTest`, `CollectionHelperTest`
- `GeopositionTest`, `OrientationTest`, `FramingTest`, `QuaternionHelperTest`
- `VulkanRootTest`, `VulkanBufferTest`, `InstanceCreationTest`, `PhysicalDeviceSelectionTest`, `LogicalDeviceCreationTest`
- `VideoPipelineTest`, `VideoMarringPipelineTest`, `VideoMarringTest`
- `CrossFormatDecoder` (image test utility)

---

## Remaining: EXTERNAL (deployment ecosystem — out of library scope per README)

1. **Install/key-provisioning flow** — generate hardware-backed Ed25519 keypair, store private key in secure hardware, store public key in app storage
2. **Device bindings** — per-platform app integration (Android CameraX/ARCore, iOS, desktop capture)
3. **Key access/persistence** — secure key lifecycle across app restarts
4. **Registrar propagation** — MAR frames → durable external ledgers (blockchain, timestamp authorities) for a referential spine
5. **Embeddings** — placeholder, unspecified

---

## Remaining: INTERNAL (library scope gaps)

1. **QR bipolar luma modulation mark** (README §13, `notes/QR.md`)
   - `MarkEmbedder` is a trivial XOR placeholder
   - Need: ZXing QR `BitMatrix` generation from MAR canonical bytes → bipolar luma modulation GLSL shader → temporal persistence (3-5 frame hold) → rolling-frame difference extraction → ZXing decoder pass
   - Target params: QR version 16-M (81×81), ECC M (15%), luma offset ±3/255, 2×2 module size

2. **Android/ARCore pipeline** (`notes/ANDROID.md`)
   - CameraX `ImageAnalysis` for raw `ImageProxy` interception
   - GPU fragment shader (GLES/Vulkan) for invisible watermark injection
   - ARCore pose integration as watermark parameter input
   - Thread orchestration: capture → worker (MAR calc) → display (blend)
   - No Android code exists

3. **Real sensor sources** (replace desktop stubs)
   - GPS/network location (DesktopPosition returns zeros)
   - Accelerometer/gyro/magnetometer orientation (DesktopOrientation.withFraming returns null)
   - Camera intrinsics / exposure metadata (none wired)
   - Each is per-platform (Android vs desktop)

4. **Video robustness testing** (`notes/VIDEO.md`)
   - Pre-/post-compression raster comparison for hash identity
   - Keyframe sync robustness characterization
   - Compression encumbrance measurement across codecs
   - Uniqueness distribution analysis ("real raster rarity")

5. **Vulkan backend stability** (`notes/VULKAN.md`)
   - Implementation passes tests but flagged "Stable?" — reliability unconfirmed
