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
- `ExposesSignature` (with `SIGNATURE_ALGORITHM = "Ed25519"`, previously hardcoded), `Signatory`, `Signs`, `VerifiesSignature`, `ExposesSigningKeys`, `CreatesSignature` — Ed25519 signing interfaces
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
- `DefaultEnvironments.desktopEnvironment()` — wires `SipMarHasher` + `Blake3MediaHasher` + `SignsForJava15` + desktop sensor stubs (ephemeral keypair per session)
- `WindowsEnvironments.windowsPermanentEnvironment()` — wires same hashers + `WindowsCredentialSignatory` for persistent keypair across restarts
- `DefaultEnvironments.partialEnvironment()` — shared hashers + time source (nulls for position/orientation/signatory), consumed by both above factories
- `BindsConstructors.defaultConstructors()` — wires all model constructors
- `BindsMarkingPipeline` — reducer + combiner + marker
- `BindsEnvironment` — record with `withPosition()`, `withOrientation()`, `withSignatory()` withers instead of static factories
- `SipMarHasher` — SipHash 4-8 via `io.whitfin:siphash`
- `Blake3MediaHasher` — BLAKE3 via `io.github.rctcwyvrn:blake3`
- `SignsForJava15` — Ed25519 via `java.security.Signature` (ephemeral keypair, no persistence)
- `PermanentSignatory` — abstract `Signatory` base: generates Ed25519 keypair on first use, reloads from persistent storage via `load/storePrivateKeyBytes` + `load/storePublicKeyBytes` hooks
- `WindowsCredentialSignatory` (`usage.defaults.windows`) — `PermanentSignatory` subclass using JNA (`Advapi32.dll`: `CredWriteW`/`CredReadW`) to store PKCS#8 private key + X.509 public key in Windows Credential Manager under MAR-named targets (`gigantspinosaurus/mar/ed25519/{priv,pub}`)
- JNA 5.18.0 (`net.java.dev.jna:jna`, `jna-platform`) — Win32 API access for credential storage

### Near-real-time video marring
- `VideoMarring` — dual-thread state machine (GRAB_FRAME→CALCULATE_MARK→APPLY_MARK→WAIT_EMPTY),
  thread coordination extracted into pluggable `VideoMarringCoordinator`
- `VideoMarringCoordinator` — interface for media/calc thread synchronization
- `BlockingMarringCoordinator` — `ReentrantLock` + `Condition`-based coordinator (blocks calc thread waiting for work)
- `NonBlockingMarringCoordinator` — lock-only coordinator (no parking, suitable for busy-polling)
- `WorkerState` — extracted enum: GRAB_FRAME, CALCULATE_MARK, APPLY_MARK, WAIT_EMPTY, BROKEN

### QR mark rendering (`usage.qr`)
- `QrSpatialMark` — CPU spatial renderer: configurable position, module pixel size, luma offset; TIM sense via frameIndex parity
- `QrMarkEmbedder` — ZXing `QRCodeWriter` wrapper: fixed Version 18-M, byte mode ISO-8859-1, MARGIN=0, byte-per-module packing

### QR mark extraction & decoding (`usage.qr` + `usage.video`)
- `RollingBufferExtractsMarks` (`usage.video`) — configurable rolling-average extraction with running integer sum
- `ZxingDecodesMar` (`usage.qr`) — ZXing `QRCodeReader` wrapper feeding mask bytes as ARGB to `HybridBinarizer`
- `VideoVerification` (`usage.video`) — streaming orchestrator: accepts `BindsExtractionPipeline`, exposes `acceptFrame(ByteBuffer, int) → Optional<byte[]>`, `flush()`, `reset()`
- `BindsExtractionPipeline` — one-shot `extractAndDecode(ByteBuffer) → Optional<byte[]>`
- `QrExtractionPipeline` (`usage.video`) — static factory: `videoTimExtraction(width, height) → BindsExtractionPipeline`

### Desktop sensor stubs (`usage.defaults.desktop`)
- `DesktopPosition` — returns zeros for all fields
- `DesktopOrientation` — fixed quaternion (1, 0, 1, 0); `withFraming` returns new instance

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
- `VideoMarringTimTest` — TIM alternating mark + extraction + ZXing decode + cryptographic verification
- `CrossFormatDecoder` (image test utility)
- `PermanentSignatoryTest` — 4 tests: generation+reload round-trip, sign+verify, wrong-payload rejection, key identity across instances

---

## Remaining: EXTERNAL (deployment ecosystem — out of library scope per README)

1. **Install/key-provisioning flow** — generate hardware-backed Ed25519 keypair, store private key in secure hardware, store public key in app storage
   - **DONE**: Windows reference implementation — `WindowsCredentialSignatory` stores Ed25519 key in Windows Credential Manager via JNA
   - **REMAINING**: macOS Keychain, Android KeyStore, iOS Keychain implementations
2. **Device bindings** — per-platform app integration (Android CameraX/ARCore, iOS, desktop capture)
3. **Key access/persistence** — secure key lifecycle across app restarts
   - **DONE**: `PermanentSignatory` abstraction + Windows Credential Manager reference
   - **REMAINING**: macOS/Android/iOS platform backings
4. **Registrar propagation** — MAR frames → durable external ledgers (blockchain, timestamp authorities) for a referential spine
5. **Embeddings** — placeholder, unspecified

---

## Remaining: INTERNAL (library scope gaps)

1. **QR bipolar luma modulation mark** (README §13, `notes/QR.md`)
   - **DONE**: `PreparesMark` — ZXing encodes Version 18-M BitMatrix from MAR canonical bytes (with MARGIN=0)
   - **DONE**: `QrSpatialMark` — CPU spatial renderer with configurable position, module size, luma offset, TIM sense
   - **DONE**: Extraction pipeline — `RollingBufferExtractsMarks` + `ZxingDecodesMar` + `BindsExtractionPipeline`
   - **DONE**: Streaming verification — `VideoVerification` orchestrator
   - **DONE**: Full integration test — `VideoMarringTimTest` (mark → extract → decode → verify)
   - **REMAINING**: Vulkan compute shader that reads the 89×89 module buffer and applies bipolar luma modulation onto the video frame at correct module positions. CPU path is sufficient for single-stream 1080p; GPU optimization deferred.

2. **Android/ARCore pipeline** (`notes/ANDROID.md`)
   - CameraX `ImageAnalysis` for raw `ImageProxy` interception
   - GPU fragment shader (GLES/Vulkan) for invisible watermark injection
   - ARCore pose integration as watermark parameter input
   - Thread orchestration: capture → worker (MAR calc) → display (blend)
   - No Android code exists

3. **Real sensor sources** (replace desktop stubs)
   - GPS/network location (DesktopPosition returns zeros)
   - Accelerometer/gyro/magnetometer orientation (DesktopOrientation.withFraming returns new instance)
   - Camera intrinsics / exposure metadata (none wired)
   - Each is per-platform (Android vs desktop)

4. **Video robustness testing** (`notes/VIDEO.md`)
   - Pre-/post-compression raster comparison for hash identity
   - Keyframe sync robustness characterization
   - Compression encumbrance measurement across codecs
   - Uniqueness distribution analysis ("real raster rarity")

5. **Vulkan backend stability** (`notes/VULKAN.md`)
   - Implementation passes tests but flagged "Stable?" — reliability unconfirmed
