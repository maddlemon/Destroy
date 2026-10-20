# usage: python port-tools/errsummary.py compile.log  -> unique javac errors, errors per package area, most common missing symbols
import re, sys, collections

seen = set(); area = collections.Counter(); sym = collections.Counter()
lines = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines()
pat = re.compile(r".*?[\\/]mc[\\/]destroy[\\/](\S+?)\.java:(\d+): error: (.*)")
for i, l in enumerate(lines):
    m = pat.match(l)
    if not m: continue
    key = m.groups()
    if key in seen: continue
    seen.add(key)
    parts = re.split(r"[\\/]", m.group(1))
    area["/".join(parts[:2]) if len(parts) > 2 else "(top level)"] += 1
    for j in range(i + 1, min(i + 5, len(lines))):
        s = re.search(r"symbol:\s+(?:class|variable|method)\s+(\w+)", lines[j])
        if s: sym[s.group(1)] += 1; break
print("unique errors:", len(seen))
print("by area:"); [print(f"  {n:5} {a}") for a, n in area.most_common(25)]
print("top missing symbols:"); [print(f"  {n:5} {s}") for s, n in sym.most_common(25)]
