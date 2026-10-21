# usage: python port-tools/compare_dumps.py <1.20.1 autotest dir> <1.21.1 autotest dir> [file ...]
# Diffs the TSV dumps written by the src/autotest driver's "dump" command. Rows are keyed by their first column
# (first two for jei_recipes / jei_ingredients, whose first column is the category / type); prints rows only in one
# version and rows whose remaining columns differ.
import os, sys
from collections import defaultdict
old_dir, new_dir = sys.argv[1], sys.argv[2]
files = sys.argv[3:] or sorted(f for f in os.listdir(new_dir) if f.endswith(".tsv"))

def load(path, keycols):
    rows = defaultdict(list)
    if not os.path.exists(path): return None
    for line in open(path, encoding="utf-8").read().splitlines():
        cols = line.split("\t")
        rows["\t".join(cols[:keycols])].append("\t".join(cols[keycols:]))
    return rows

for f in files:
    keycols = 2 if f in ("jei_recipes.tsv", "jei_ingredients.tsv") else 1
    old, new = load(os.path.join(old_dir, f), keycols), load(os.path.join(new_dir, f), keycols)
    print(f"===== {f} =====")
    if old is None or new is None: print("  missing on one side"); continue
    only_old = sorted(set(old) - set(new)); only_new = sorted(set(new) - set(old))
    changed = [k for k in sorted(set(old) & set(new)) if sorted(old[k]) != sorted(new[k])]
    print(f"  {len(old)} keys in 1.20.1, {len(new)} in 1.21.1: {len(only_old)} only 1.20.1, {len(only_new)} only 1.21.1, {len(changed)} changed")
    for k in only_old: print(f"  - {k}\t{' || '.join(old[k])}")
    for k in only_new: print(f"  + {k}\t{' || '.join(new[k])}")
    for k in changed:
        print(f"  ~ {k}")
        for v in sorted(set(old[k]) - set(new[k])): print(f"      1.20.1: {v}")
        for v in sorted(set(new[k]) - set(old[k])): print(f"      1.21.1: {v}")
