import os, glob
from PIL import Image

AVIF_DIR = "src/test/resources/cross-format"
avif_files = sorted(glob.glob(os.path.join(AVIF_DIR, "*.avif")))

print(f"Found {len(avif_files)} AVIF files\n")

created = 0
for src in avif_files:
    out = src + ".png"
    if os.path.exists(out):
        print(f"  SKIP (exists): {os.path.basename(out)}")
        continue
    im = Image.open(src).convert("RGB")
    im.save(out, format="PNG")
    print(f"  OK: {os.path.basename(src)} -> {os.path.basename(out)} ({im.size[0]}x{im.size[1]})")
    created += 1

print(f"\nDone. {created} file(s) created.")
