# QR Mark Scope

## Approach

MAR canonical bytes are encoded into a **fixed Version 18-M** QR code via ZXing. The resulting BitMatrix (89×89 modules) is packed into the mark buffer for GPU upload. All ZXing logic is encapsulated inside `PreparesMark` — nothing outside touches it.

## Key Decisions

- **Fixed Version 18-M**: Known geometry (89×89 modules) means the GLSL shader has compile-time constants for module addressing. No runtime version selection, no geometry back-derivation.
- **Raw binary in byte mode**: No BASE64 or other transcoding — MAR canonical bytes go directly into QR byte mode via ISO-8859-1 encoding.
- **`emptyMark(int marCanonicalSize)`**: Allocates a direct ByteBuffer of 7921 bytes (89×89, 1 byte per module). The `marCanonicalSize` parameter is accepted for interface consistency and potential future capacity assertions. Buffer size is fixed.
- **`accept(marBytes, markBuffer)`**: ZXing `QRCodeWriter.encode()` with version=18, ECC=M, character set ISO-8859-1, MARGIN=0 (quiet zone handled by the spatial renderer). BitMatrix is packed as byte-per-module: 255 for black, 0 for white.

## Pipeline

1. `VideoMarring` constructor: create `MarCreation`, produce intent frame, measure `intent.canonicalBytes().length`, call `emptyMark(marSize)` → direct ByteBuffer(7921)
2. Once per new intent frame: ZXing encodes `mar.canonicalBytes()` → `BitMatrix(89×89)` → packed into mark buffer via `accept(marBytes, markBuffer)`
3. `MarksMedia.mark(mark, target, frameIndex)`: spatially renders QR module data onto the video frame with bipolar luma modulation. `frameIndex` parity determines TIM sense (even → PLUS, odd → MINUS).

## Parameters

| Variable | Value |
|---|---|
| QR Version | 18 |
| ECC Level | M (15%) |
| Modules | 89×89 |
| Data mode | Byte |
| Mark buffer size | 7921 bytes (1 byte/module) |
| Module values | 255 (black), 0 (white) |
| Luma Offset | ±3/255 (1.2%) |
| ZXing MARGIN | 0 (quiet zone added by spatial renderer) |
| TIM sense | even frameIndex → PLUS, odd → MINUS |
| Persistence | 3-5 frames |

## Temporal Persistence

To make the QR signal recoverable via extraction, the same QR pattern must persist long enough for the extraction algorithm to distinguish it from video motion.

- **Minimum Duration**: Keep the same QR pattern for at least 3-5 frames (at 30/60 fps).
- **Transition Handling**: Hard cuts only — no cross-fade between QR textures. Mixed-data frames would fail CRC checks.
- **Asserted Index**: The MAR frame includes the index in its payload. The extractor uses a voting buffer: if the same index is decoded from three consecutive frame-groups, it accepts that data as "True."

## Extraction Pipeline

`RollingBufferExtractsMarks` — configurable rolling-average mark extraction:

1. **Frame Capture**: Store the last N frames (default 5) in a circular byte buffer of per-pixel luma values.
2. **Running Average**: Maintain a running integer sum; compute `avg = sum / N` each frame.
3. **Difference & Threshold**: `|currentLuma - avg| > threshold` → pixel is QR-modulated (mask byte = 0), otherwise background (= 255). TIM alternation (±3 offset) cancels in the average, leaving detectable difference at QR positions.
4. **Mask polarity**: 0 = QR black matches ZXing finder-pattern expectations (black-on-white).

`ZxingDecodesMar` — wraps ZXing `QRCodeReader`:

1. Converts mask bytes (0/255) to ARGB int array.
2. Feeds `RGBLuminanceSource` + `HybridBinarizer` to `QRCodeReader.decode()`.
3. Returns decoded MAR canonical bytes via ISO-8859-1, or empty array on failure.

`VideoVerification` — streaming orchestrator:

- `acceptFrame(ByteBuffer, int) → Optional<byte[]>`: extract + decode + deduplicate by MAR index.
- `flush()`: (reserved for pending frames).
- `reset()`: clears last-decoded-index guard.

`BindsExtractionPipeline` — one-shot convenience:

- `extractAndDecode(ByteBuffer) → Optional<byte[]>`: creates a fresh extractor per call (suitable when no rolling state is needed).

## CPU Spatial Rendering (`QrSpatialMark`)

The current `MarksMedia` implementation is `QrSpatialMark` — a pure-Java spatial renderer:

- Configurable: `lumaOffset`, `modulePixels` (pixels per QR module), `offsetX/Y`, `frameWidth/Height`.
- `autoFit(lumaOffset, w, h)`: centers QR on frame with `computeModulePixels()` = `Math.min(w, h) / 89` minus margin.
- For each black module (mark byte ≠ 0): draws `modulePixels × modulePixels` block adjusting all RGB channels by ±`lumaOffset`.
- TIM sense: `(frameIndex % 2 == 0) → PLUS (+offset)`, else `MINUS (-offset)`.
- Channel clamping: `Math.min(255, c + offset)` / `Math.max(0, c - offset)`.

### Cost

1080p auto-fit → `modulePixels ≈ 12` → ~1.14M pixels/frame, ~1-3ms on CPU.

## GPU Modulation (future optimization)

When multi-stream or 4K+ becomes the target, replace `QrSpatialMark` with a Vulkan compute shader as a 6th pipeline stage after the reduction pass. The shader logic is straightforward:

```glsl
vec4 tex = texture2D(videoTexture, vTexCoord);
float qr = texture2D(qrTexture, qrCoord).r; // 1.0 for Black module, 0.0 for White
float mask = (tex.r + tex.g + tex.b) / 3.0;
float adaptiveOffset = lumaOffset * clamp(mask, 0.2, 0.8);
float mod = (qr > 0.5) ? -adaptiveOffset : adaptiveOffset;
gl_FragColor = vec4(tex.rgb + mod, tex.a);
```

Not justified at single-stream 1080p — CPU path is adequate. Revisit when throughput demands increase.

## Summary of Changes from Prior Approach

| Previous (QR.md v1) | Current |
|---|---|
| QR Version 16-M, 600-byte payload | Version 18-M fixed, 89×89 modules |
| Fixed `MARK_SIZE = 600` in code | Buffer sized to BitMatrix (7921 bytes) |
| `emptyMark()` — no parameter | `emptyMark(int marCanonicalSize)` |
| ZXing not yet used | ZXing `QRCodeWriter` inside `PreparesMark.accept()` |
| 600-byte constant coupled to version | Version is explicit, no magic capacity constant |
| XOR placeholder only | CPU spatial renderer (`QrSpatialMark`) with configurable position, module size, luma offset |
| `mark(mark, target)` no index | `mark(mark, target, frameIndex)` — parity drives TIM sense |
| No extraction pipeline | `RollingBufferExtractsMarks` + `ZxingDecodesMar` + `VideoVerification` |
| GLSL shader listed as "planned" | Demoted to future optimization; CPU path sufficient for now |
