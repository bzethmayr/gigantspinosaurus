We have to reduce the video to a form that survives compression while retaining very high uniqueness.

```text
ALL INTEGER or similarly portable replicability
Color space conversion (RGB → YCbCr)
Chroma subsampling (4:2:0)
Block partitioning (8×8, 16×16, etc.)
DCT or wavelet transform
Quantization (lossy)
Entropy coding
```

We perform a DWT first, at resolution less than or equal to the compressor resolution,
taking a size-dependent sample of the low-frequency quadrant.
We downsample post-DWT in another shader: using bilinear filter with integer math only to a 160×90 texture.
We then perform a 12x12 "feature" grid, detecting the strongest gradient's rough point of origin and orientation in each such cell:
* we perform Sobel gradient detection over the cell
  ```
  gx = (p2 + 2*p5 + p8) - (p0 + 2*p3 + p6)
  gy = (p0 + 2*p1 + p2) - (p6 + 2*p7 + p8)
  mag = abs(gx) + abs(gy)
  mag >>= k   // your blunt instrument
  ```
We are hoping for a real raster of size at least 96x96, but still need to be consistently wrong on smaller raster sizes.

```
11001100
strength (2 bits)
  origin-x (2 bits)
    origin-y (2 bits)
      orientation (2 bits)
```
at moderate resolution, discarding any gradient weak enough to have been triggered by a compressor macroblock.

We should probably sync to keyframes. If we thought it were actually robust to sync to macroblocks we'd do that too.
We don't know yet whether it's robust to sync to keyframes.

# development crank
* Video abstraction
    * AR Frame compatibility
* Test video
    * interface level
    * raster access
    * real compression encumbrance
    * real raster rarity
* GL environment
    * abstracted env
    * degree of validity

Needs to:
* obtain "real" raster
    * perform reduction and hash
* obtain post-compression raster
    * perform reduction and hash
* establish identity
* for a sample large unto representation

