# usage: python port-tools/compare_gameplay.py <1.20.1 gameplay log.txt> <1.21.1 gameplay log.txt>
# Reduces each "BE x y z ..." / "ITEMS ..." line of the autotest gameplay log to the item and fluid stacks it holds
# (1.20.1 Count/FluidName/Amount and 1.21.1 count/id/amount NBT both map to "id xN"), then prints both versions per
# checkpoint so differences stand out.
import re, sys

SKIP = {"create:fluid_tank", "create:depot", "create:basin", "minecraft:empty", "minecraft:air"}

def stacks(nbt):
    out = []
    # 1.20.1 fluids: Amount:N,FluidName:"x"   items: Count:Nb,...id:"x"
    for amount, fluid in re.findall(r'Amount:(\d+),FluidName:"([^"]+)"', nbt): out.append((fluid, int(amount)))
    for count, item in re.findall(r'Count:(\d+)b,(?:Slot:\d+b?,)?id:"([^"]+)"', nbt): out.append((item, int(count)))
    # 1.21.1 fluids: Fluid:{amount:N,...,id:"x"}   items: {count:N,id:"x"} / {Slot:N,count:N,id:"x"}
    for amount, fluid in re.findall(r'Fluid:\{amount:(\d+),(?:components:\{.*?\}\},)?id:"([^"]+)"', nbt): out.append((fluid, int(amount)))
    for count, item in re.findall(r'count:(\d+),id:"([^"]+)"', nbt): out.append((item, int(count)))
    molecules = sorted(set(re.findall(r'Molecule:"([^"]+)"', nbt)))
    return sorted(s for s in out if s[0] not in SKIP and s[1] > 0), molecules

def load(path):
    rows, checkpoint = {}, "setup"
    for line in open(path, encoding="utf-8"):
        line = line.rstrip("\n")
        if re.match(r"^t\+\d+$", line) or line == "inputs": checkpoint = line; continue
        m = re.match(r"^BE (-?\d+ -?\d+ -?\d+) (Block\{[^}]+\}(?:\[[^\]]*\])?) ?(.*)$", line)
        if m:
            s, mol = stacks(m.group(3))
            rows[(checkpoint, m.group(1))] = f"{m.group(2).split('}')[0][6:]}  {s}" + (f"  molecules={mol}" if mol else "")
            continue
        m = re.match(r"^ITEMS (\S+ \S+ \S+ \S+) (.*)$", line)
        if m: rows[(checkpoint, "items " + m.group(1))] = m.group(2)
    return rows

a, b = load(sys.argv[1]), load(sys.argv[2])
for key in sorted(set(a) | set(b), key=lambda k: (k[0] != "setup", int(re.sub(r"\D", "", k[0]) or 0), k[1])):
    va, vb = a.get(key, "-"), b.get(key, "-")
    mark = "  " if va == vb else "!!"
    print(f"{mark} [{key[0]}] {key[1]}\n     1.20.1: {va}\n     1.21.1: {vb}")
