# usage: python port-tools/at_check.py src/main/resources/META-INF/accesstransformer.cfg build/moddev/artifacts/neoforge-21.1.262-merged.jar
# Prints AT entries whose class/member does not exist in the given jar (needs JAVA_HOME = JDK 21).
import re, subprocess, sys, os
at, jar = sys.argv[1], sys.argv[2]
javap = os.path.join(os.environ["JAVA_HOME"], "bin", "javap")
cache = {}
def members(cls):
    if cls not in cache:
        r = subprocess.run([javap, "-p", "-s", "-cp", jar, cls], capture_output=True, text=True)
        cache[cls] = r.stdout if r.returncode == 0 else None
    return cache[cls]
for n, line in enumerate(open(at, encoding="utf-8"), 1):
    line = line.split("#")[0].strip()
    if not line: continue
    parts = line.split()
    cls = parts[1].replace("/", ".")
    out = members(cls)
    if out is None:
        print(f"{n}: MISSING CLASS {cls}"); continue
    if len(parts) < 3: continue
    mem = parts[2]
    if "(" in mem:
        name, desc = mem.split("(", 1); desc = "(" + desc
        ok = False; lines = out.splitlines()
        for i, l in enumerate(lines):
            if re.search(r"[ .]" + re.escape(name) + r"\(", l) and i + 1 < len(lines) and desc in lines[i+1]:
                ok = True; break
        if not ok:
            cands = [l.strip() for l in lines if re.search(r"[ .]" + re.escape(name) + r"\(", l)]
            print(f"{n}: MISSING METHOD {cls}.{mem}\n    have: {cands}")
    else:
        if not re.search(r"\s" + re.escape(mem) + r";", out):
            print(f"{n}: MISSING FIELD {cls}.{mem}")
