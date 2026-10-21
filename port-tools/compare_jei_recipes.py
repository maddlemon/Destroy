# usage: python port-tools/compare_jei_recipes.py <1.20.1 jei_recipes.tsv> <1.21.1 jei_recipes.tsv>
# Compares JEI recipes by their slot contents only (recipe ids differ: the port wraps runtime recipes in synthetic holders).
import re, sys
from collections import Counter, defaultdict
def load(p):
    d = defaultdict(Counter)
    for line in open(p, encoding="utf-8").read().splitlines():
        cat, _id, slots = (line.split("\t") + ["", ""])[:3]
        # slot order within a role and ingredient order within a slot aren't meaningful
        parts = re.findall(r"([IOCRN])\[(.*?)\](?= [IOCRN]\[|$)", slots)
        d[cat]["  ".join(sorted(role + "[" + ",".join(sorted(re.split(r",(?=[\w.+-]+:|\+\d)", ings))) + "]" for role, ings in parts))] += 1
    return d
old, new = load(sys.argv[1]), load(sys.argv[2])
for cat in sorted(set(old) | set(new)):
    o, n = old.get(cat, Counter()), new.get(cat, Counter())
    if o == n: continue
    lost, gained = o - n, n - o
    print(f"== {cat}: {sum(o.values())} -> {sum(n.values())} ({sum(lost.values())} only 1.20.1, {sum(gained.values())} only 1.21.1)")
    for k, v in sorted(lost.items())[:int(sys.argv[3]) if len(sys.argv) > 3 else 6]: print(f"   - {k[:400]}" + (f" x{v}" if v > 1 else ""))
    for k, v in sorted(gained.items())[:int(sys.argv[3]) if len(sys.argv) > 3 else 6]: print(f"   + {k[:400]}" + (f" x{v}" if v > 1 else ""))
