# usage: python port-tools/sbs_screens.py <1.20.1 screenshots dir> <1.21.1 screenshots dir> <out dir>
# Side-by-side composites (1.20.1 left, 1.21.1 right) of same-named screenshots, plus a ranking by mean pixel difference.
import os, sys
from PIL import Image, ImageChops, ImageDraw, ImageStat
a, b, out = sys.argv[1:4]
os.makedirs(out, exist_ok=True)
scores = []
for name in sorted(set(os.listdir(a)) & set(os.listdir(b))):
    ia, ib = Image.open(os.path.join(a, name)).convert("RGB"), Image.open(os.path.join(b, name)).convert("RGB")
    if ia.size != ib.size: ib = ib.resize(ia.size)
    diff = sum(ImageStat.Stat(ImageChops.difference(ia, ib)).mean) / 3
    scores.append((diff, name))
    w, h = ia.size
    sbs = Image.new("RGB", (w * 2 + 8, h + 24), (40, 40, 40))
    sbs.paste(ia, (0, 24)); sbs.paste(ib, (w + 8, 24))
    d = ImageDraw.Draw(sbs); d.text((8, 6), "1.20.1  " + name, fill=(255, 255, 255)); d.text((w + 16, 6), "1.21.1", fill=(255, 255, 255))
    sbs.resize((sbs.width // 2, sbs.height // 2)).save(os.path.join(out, name))
for s, n in sorted(scores, reverse=True): print(f"{s:6.1f}  {n}")
