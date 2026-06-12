# Changelog

All notable changes to this project are documented here.

The library version is independent of the MAR format version (see notes below).

---

## 0.6.4-SNAPSHOT (2026-06-12)

- `SIGNATURE_ALGORITHM = "Ed25519"` constant extracted to `ExposesSignature`;
  hardcoded `"Ed25519"` literals replaced in `SignsForJava15`,
  `PermanentSignatory`, `BindsEnvironment`.
- JNA 5.18.0 (`net.java.dev.jna:jna`, `jna-platform`) added for Win32 API access.
- `PermanentSignatory` — abstract `Signatory` base class with Ed25519 key
  generation and `load/storePrivateKeyBytes` / `load/storePublicKeyBytes`
  persistence hooks.
- `WindowsCredentialSignatory` (`usage.defaults.windows`) — `PermanentSignatory`
  subclass using JNA `Advapi32.dll` (`CredWriteW`/`CredReadW`) to store PKCS#8
  private + X.509 public key in Windows Credential Manager under MAR-named
  targets (`gigantspinosaurus/mar/ed25519/{priv,pub}`).
- `BindsEnvironment` refactored: static factories (`desktopEnvironment()`,
  `windowsPermanentEnvironment()`) extracted to dedicated factory classes;
  `withPosition()`, `withOrientation()`, `withSignatory()` withers added.
- `DefaultEnvironments` — factory utility providing `partialEnvironment()`
  (shared hashers + time source) and `desktopEnvironment()`.
- `WindowsEnvironments` — factory utility providing
  `windowsPermanentEnvironment()`.
- `usage.desktop` package moved to `usage.defaults.desktop`.
- `PermanentSignatoryTest` — 4 tests covering generation+reload round-trip,
  sign+verify, wrong-payload rejection, key identity across instances.

## 0.6.3-SNAPSHOT (2026-06-11)

- Coordinator refactoring: `BlockingMarringCoordinator` and
  `NonBlockingMarringCoordinator` extracted from `VideoMarring`.
- `WorkerState` enum extracted: `GRAB_FRAME`, `CALCULATE_MARK`, `APPLY_MARK`,
  `WAIT_EMPTY`, `BROKEN`.

## 0.6.2-SNAPSHOT (2026-06-05)

- QR mark pipeline complete:
  - `PreparesMark` — ZXing encodes Version 18-M BitMatrix from MAR canonical bytes
  - `QrSpatialMark` — CPU spatial renderer with configurable position, module size,
    luma offset, TIM sense via frameIndex parity
  - `RollingBufferExtractsMarks` — rolling-average mark extraction
  - `ZxingDecodesMar` — ZXing `QRCodeReader` wrapper feeding mask bytes as ARGB
  - `VideoVerification` — streaming verification orchestrator
  - `BindsExtractionPipeline` / `QrExtractionPipeline` — wiring
- Full integration test: `VideoMarringTimTest` (mark → extract → decode → verify)

## 0.6.1-SNAPSHOT (2026-06-04)

- QR marking scope started.
- `QrMarkEmbedder` — ZXing `QRCodeWriter` wrapper, fixed Version 18-M, byte mode.
- `emptyMark(int marCanonicalSize)` / `accept(marBytes, markBuffer)` contract.

## 0.6.0-SNAPSHOT (2026-05-31)

- Protocol versioning rules for canonical attribute serialization.
- `Versioned` interface: all canonical model types carry a `ver` field.
- Version ack across all model records.

## 0.5.3-SNAPSHOT (2026-05-26)

- Test image corpus (`src/test/resources`) with WEBP, JPEG2000 support.
- Fixes frame interlock and framing source wiring.
- Cleanups, dropped AVIF format support.

## 0.5.2-SNAPSHOT (2026-05-15)

- GPU abstraction interfaces: `GpuContext`, `GpuBuffer`, `GpuProgram`,
  `GpuProgramLoan`, `GpuJobSpec`, `SpecifiesGpuJob`, `UsesGpuProgram`.
- Vulkan backend: buffer operations (`VulkanBuffer`), queue selection,
  command pool creation.
- HLSL shader development: `downsample.hlsl`, `dwt_ll.hlsl`, `sobel_feature.hlsl`,
  `cell_packing.hlsl`.
- `ycbcr_reduction.hlsl` — RGB→luma via integer math.

## 0.5.1-SNAPSHOT (2026-05-04)

- Vulkan through logical device creation.
- `ClosingChain` for deterministic resource disposal.
- Shadow JAR packaging (`shadowJar`).
- Reorganizing for clarity of LWJGL vs local surface.

## 0.5.0-SNAPSHOT (2026-04-30)

- Vulkan backend bootstrap: `InstanceCreation`, `PhysicalDeviceSelection`,
  `PhysicalDeviceMetadata`, `LogicalDeviceCreation`, `QueueSelection`.
- `VulkanRoot` — owns instance, device, queue, command pool lifecycle.
- Scoring-based selection for physical devices and queue families.

## 0.4.0-SNAPSHOT (2026-04-21)

- **1.x series burned.** Renumbered to 0.x for Maven readiness.
- Environment binding: `BindsEnvironment` record wires hashers, signers, sensors.
- Media reduction pipeline: `ReductionStep`, `ReductionIds`, `ReducesMedia`.
- YCbCr separable reduction (`ColorSpaceReduction`).
- HLSL→SPIR-V toolchain via DXC (`compileHlsl` Gradle task).
- LWJGL 3.3.3→3.4.1, Vulkan Memory Allocator added.
- GPU abstraction sketched.

---

## 1.x series (burned)

Renumbered to 0.x at the 0.4.0 boundary. Retained here for history.

### 1.3.0-SNAPSHOT (2026-04-21)

- DXC vendored for HLSL→SPIR-V compilation.
- LWJGL licensing acknowledged.
- GPU abstraction started.

### 1.2.0-SNAPSHOT (2026-04-15)

- BLAKE3 introduced as media hasher (`Blake3MediaHasher`,
  `io.github.rctcwyvrn:blake3:1.3`).
- Default media hash, stronger mocking, test exclusions removed.
- JaCoCo coverage at 80% line threshold.

### 1.1.1-SNAPSHOT (2026-04-14)

- Fixing the docs finds bugs too. Inverted sentences in README.

### 1.1.0-SNAPSHOT (2026-04-13)

- Desktop environment binding: `BindsEnvironment.desktopEnvironment()`.
- `Blake3MediaHasher`, `SipMarHasher`, `SignsForJava15` as defaults.
- Protocol version bumped.

### 1.0.3-SNAPSHOT (2026-04-09)

- No more optional fields in MAR model types.
- Canonical serialization roundtripping.
- `Ed25519` signature lengths fixed.

### 1.0-SNAPSHOT (2026-03-19)

- Initial commit.
- MAR model types: `MinimalAttestationRecord`, `ExposesMar`, `Geoposition`,
  `Orientation`, `Media`, `MarSignature`.
- Canonical serialization: `HasCanonicalAttributes`, `BoundAttributes`.
- SipHash 4-8 chaining, Ed25519 signing.
- Creation (`MarCreation`) and verification (`MarVerification`).

---

## Component version history

Each `Versioned` entity in the canonical serialization model carries its own
independent version number. The MAR wire format is `MAR_VERSION` below.

### `MAR_VERSION` — `ExposesMar`

| Version | Library range | Changes |
|---------|---------------|---------|
| 2 | 1.0.0 | Initial — `HasRequiredAttributes` era |
| 3 | 1.0.3 | No more optional fields. `HasRequiredAttributes`→`HasCanonicalAttributes` |
| 4 | 1.1.0 | Hash fields renamed to `previous/currentSipH4_8` |
| 5 | 1.1.1 | Hash fields renamed to `prev/curr_Mxx64_FsipH4_8` |
| 6 | 1.2.0–1.3.x | Adds `mediaBLK3` field, back to `prior/currentSipH4_8` naming |
| **7** | **0.4.0+** | Removes `mediaBLK3`, adds `media` object with `ReductionStep[]` + `BLK3` |

### `POSITION_VERSION` — `ExposesPosition`

| Version | Introduced | Changes |
|---------|------------|---------|
| 1 | 27d1c9f (lib 1.0.0) | Initial |
| **2** | **eaa2410 (lib 1.0.3)** | Added `north` field; `HasRequiredAttributes`→`HasCanonicalAttributes` |

### `ORIENTATION_VERSION` — `ExposesOrientation`

| Version | Introduced | Changes |
|---------|------------|---------|
| **1** | **27d1c9f (lib 1.0.0)** | Unchanged since introduction |

### `FRAMING_VERSION` — `ExposesFraming`

| Version | Introduced | Changes |
|---------|------------|---------|
| 3 | 27d1c9f (lib 1.0.0) | Initial via `HasRequiredAttributes` |
| **4** | **7d21a3c (lib 1.0.3)** | `HasRequiredAttributes`→`HasCanonicalAttributes` |

### `SIGNATURE_VERSION` — `ExposesSignature`

| Version | Introduced | Changes |
|---------|------------|---------|
| 0 | 27d1c9f (lib 1.0.0) | 128-byte pub key, 256-byte signature |
| 0→0 | e4050f0→d072610 (lib 1.0.0) | Key shrunk 128→64→32 bytes, signature 256→128→64 bytes |
| 2 | 7d21a3c (lib 1.0.3) | 32-byte key, 64-byte sig, v=2. `HasRequiredAttributes`→`HasCanonicalAttributes` |
| **3** | **ef44147 (lib 1.2.0)** | X509-encoded 44-byte pub key, 64-byte sig |

### `MEDIA_VERSION` — `ExposesMedia`

| Version | Introduced | Changes |
|---------|------------|---------|
| 0 | `6bf77f1` (lib 0.4.0) | Initial. Created alongside MAR v7. |
| **1** | **272b4a1 (lib 0.6.0)** | Protocol versioning rules acked across all model types. |

### `REDUCTION_STEPS` — Known reduction step identifiers

| ID | Name | Version | Introduced | Changes |
|----|------|---------|------------|---------|
| 0 | (sentinel) | -1 | `6bf77f1` (lib 0.4.0) | No-reduction sentinel. Not a real reducer. |
| 1 | YCbCr (luma) | 0 | `017ba09` (lib 0.5.x) | Initial. RGB→luma via integer math. |
| 2 | Spatial (DWT+Sobel) | 0 | `017ba09` (lib 0.5.x) | Initial. DWT LL subband + Sobel features. |
