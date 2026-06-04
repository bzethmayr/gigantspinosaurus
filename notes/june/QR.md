# QR Mark Scope

## Approach

MAR canonical bytes are encoded into a **fixed Version 18-M** QR code via ZXing. The resulting BitMatrix (89×89 modules) is packed into the mark buffer for GPU upload. All ZXing logic is encapsulated inside `PreparesMark` — nothing outside touches it.

## Key Decisions

- **Fixed Version 18-M**: Known geometry (89×89 modules) means the GLSL shader has compile-time constants for module addressing. No runtime version selection, no geometry back-derivation.
- **Raw binary in byte mode**: No BASE64 or other transcoding — MAR canonical bytes go directly into QR byte mode via ISO-8859-1 encoding.
- **`emptyMark(int marCanonicalSize)`**: Allocates a direct ByteBuffer of 7921 bytes (89×89, 1 byte per module). The `marCanonicalSize` parameter is accepted for interface consistency and potential future capacity assertions. Buffer size is fixed.
- **`accept(marBytes, markBuffer)`**: ZXing `QRCodeWriter.encode()` with version=18, ECC=M, character set ISO-8859-1. BitMatrix is packed as byte-per-module: 255 for black, 0 for white.

## Pipeline

1. `VideoMarring` constructor: create `MarCreation`, produce intent frame, measure `intent.canonicalBytes().length`, call `emptyMark(marSize)` → direct ByteBuffer(7921)
2. Per-frame calculation: ZXing encodes `mar.canonicalBytes()` → `BitMatrix(89×89)` → packed into mark buffer via `accept(marBytes, markBuffer)`
3. `MarksMedia.mark(mark, target)`: blends QR module data into video frame (current: XOR; planned: GLSL bipolar luma modulation)

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
| Persistence | 3-5 frames |

## Temporal Persistence

To make the QR signal recoverable via extraction, the same QR pattern must persist long enough for the extraction algorithm to distinguish it from video motion.

- **Minimum Duration**: Keep the same QR pattern for at least 3-5 frames (at 30/60 fps).
- **Transition Handling**: Hard cuts only — no cross-fade between QR textures. Mixed-data frames would fail CRC checks.
- **Asserted Index**: The MAR frame includes the index in its payload. The extractor uses a voting buffer: if the same index is decoded from three consecutive frame-groups, it accepts that data as "True."

## Extraction Strategy

1. **Frame Capture**: Store the last 5 frames in a circular buffer.
2. **Noise Reduction**: Subtract the current frame from the average of the buffer to "pop" the static QR pattern.
3. **Thresholding**: Apply a global histogram threshold to the result.
4. **ZXing Decoding**: Pass the resulting 1-bit image to ZXing QRCodeReader with HybridBinarizer.

## GLSL Modulation (planned)

```glsl
vec4 tex = texture2D(videoTexture, vTexCoord);
float qr = texture2D(qrTexture, qrCoord).r; // 1.0 for Black module, 0.0 for White
float mask = (tex.r + tex.g + tex.b) / 3.0;
float adaptiveOffset = lumaOffset * clamp(mask, 0.2, 0.8);
float mod = (qr > 0.5) ? -adaptiveOffset : adaptiveOffset;
gl_FragColor = vec4(tex.rgb + mod, tex.a);
```

## Summary of Changes from Prior Approach

| Previous (QR.md v1) | Current |
|---|---|
| QR Version 16-M, 600-byte payload | Version 18-M fixed, 89×89 modules |
| Fixed `MARK_SIZE = 600` in code | Buffer sized to BitMatrix (7921 bytes) |
| `emptyMark()` — no parameter | `emptyMark(int marCanonicalSize)` |
| ZXing not yet used | ZXing `QRCodeWriter` inside `PreparesMark.accept()` |
| 600-byte constant coupled to version | Version is explicit, no magic capacity constant |
