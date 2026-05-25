import os, sys, glob
from PIL import Image

SRC_DIR = "src/test/resources/kodak_d65"
OUT = "src/test/resources/cross-format"

QUALITIES = [95, 50, 20]
DWT_QUALITIES = [50, 20]  # exclude 95_jp2

RATE_MAP = {95: 0.5, 50: 5.0, 20: 20.0}

os.makedirs(OUT, exist_ok=True)

targets = sorted(glob.glob(os.path.join(SRC_DIR, "IMG*.tif")))
print(f"Processing {len(targets)} source images...\n")

total_created = 0

for src_path in targets:
    base = os.path.splitext(os.path.basename(src_path))[0]
    im = Image.open(src_path).convert("RGB")
    print(f"[{base}] ({im.size[0]}x{im.size[1]})")

    created = 0

    # Lossless: PNG only
    im.save(os.path.join(OUT, f"{base}_lossless.png"), format="PNG")
    created += 1

    # DCT: JPEG, WebP, AVIF at all qualities
    for fmt, ext in [("JPEG", "jpg"), ("WEBP", "webp"), ("AVIF", "avif")]:
        for q in QUALITIES:
            kw = {"format": fmt, "quality": q}
            if fmt == "WEBP":
                kw["lossless"] = False
            fname = f"{base}_dct_{q}.{ext}"
            im.save(os.path.join(OUT, fname), **kw)
            created += 1

    # DWT: JPEG2000 at 50, 20 only
    for q in DWT_QUALITIES:
        fname = f"{base}_dwt_{q}.jp2"
        im.save(os.path.join(OUT, fname), format="JPEG2000",
                quality_mode="rates", quality_layers=[RATE_MAP[q]],
                irreversible=True)
        created += 1

    total_created += created
    print(f"  -> {created} files\n")

print(f"Done. {total_created} total files in {OUT}/")
