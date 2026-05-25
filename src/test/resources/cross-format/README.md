# Cross-Format Kodak Dataset

Transcoded from the [Kodak PhotoCD dataset](https://www.math.purdue.edu/~lucier/PHOTO_CD)
to support development of **format-independent image signatures**.

## Purpose

Produce a set of visually identical images encoded across multiple
compression families so that a candidate signature algorithm can be
evaluated for format invariance — the ability to recognise the same
source image regardless of container or codec.

## Filename convention

```
IMG{NNNN}_{category}_{quality}.{ext}
```

| Field      | Meaning |
|------------|---------|
| `IMG{NNNN}` | Source image number (0001–0024) from the Kodak set |
| `{category}` | `lossless`, `dct`, or `dwt` |
| `{quality}` | Quality parameter passed to the encoder (95, 50, 20) |
| `{ext}` | File extension per format |

Lossless files use `lossless` as the category with no quality suffix.

## Included representations

| Category | Compression | Formats | Qualities |
|----------|-------------|---------|-----------|
| Lossless | — | PNG | — |
| DCT | JPEG | `.jpg` | 95, 50, 20 |
| DCT | WebP (lossy) | `.webp` | 95, 50, 20 |
| DCT | AVIF | `.avif` | 95, 50, 20 |
| DWT | JPEG 2000 | `.jp2` | 50, 20 |

AVIF and WebP use block-transform (DCT-like) coding; JPEG 2000 uses
wavelet (DWT) coding. PNG is included as a pixel-exact reference.

All lossy variants encode the same 3072 × 2048 pixel RGB source
(eight landscape, six portrait) at the indicated quality setting.

## Generation

Recreated on demand with:

```
python tools/cross_format_convert.py
```

## Source provenance

The original Kodak PhotoCD images are uncompressed TIFF files
(`kodak_d65/IMG{NNNN}.tif`), obtained from the Purdue University
mirror linked above. The D65 white-point variant is used.
