# usage: python port-tools/mixin_check.py <classpath jars...>
# Static check of Destroy's mixins: for each mixin listed in destroy.mixins.json, find the target class(es) in the given
# jars and report injector / shadow / accessor / overwrite member names the target does not have. Catches mixins written
# against 1.20.1 signatures before a game launch does (needs JAVA_HOME = JDK 21 for javap).
import json, os, re, subprocess, sys
jars = sys.argv[1:]
cp = os.pathsep.join(jars)
javap = os.path.join(os.environ["JAVA_HOME"], "bin", "javap")
cfg = json.load(open("src/main/resources/destroy.mixins.json"))
pkg = cfg["package"]
names = cfg.get("mixins", []) + cfg.get("client", []) + cfg.get("server", [])
cache = {}
def members(cls):
    if cls not in cache:
        r = subprocess.run([javap, "-p", "-cp", cp, cls], capture_output=True, text=True)
        cache[cls] = None if r.returncode else r.stdout
    return cache[cls]
problems = 0
for n in names:
    path = os.path.join("src/main/java", *(pkg + "." + n).split(".")) + ".java"
    if not os.path.exists(path): print("MISSING FILE", n); problems += 1; continue
    src = open(path, encoding="utf-8").read()
    m = re.search(r"@Mixin\s*\(([^)]*)\)", src, re.S)
    if not m: continue
    arg = m.group(1)
    targets = re.findall(r'targets\s*=\s*\{?\s*"([^"]+)"', arg) or [c + "" for c in re.findall(r"([\w.$]+)\.class", arg)]
    # resolve simple class names through imports
    resolved = []
    for t in targets:
        if "." in t and not t[0].isupper(): resolved.append(t.replace("/", ".")); continue
        outer = t.split(".")[0]
        im = re.search(r"import\s+([\w.]+\." + re.escape(outer) + r")\s*;", src)
        full = (im.group(1) if im else outer) + t[len(outer):].replace(".", "$")
        resolved.append(full)
    wanted = set()
    for ann, val in re.findall(r"@(Inject|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyConstant|ModifyReturnValue|WrapOperation|WrapWithCondition|ModifyExpressionValue|WrapMethod)\s*\((.*?)\)\s*\n\s*(?:@|public|private|protected|static|[A-Za-z])", src, re.S):
        for meth in re.findall(r'method\s*=\s*\{?\s*"([^"]+)"', val) + re.findall(r'method\s*=\s*\{[^}]*\}', val):
            for name in re.findall(r'"([^"(]+)', meth if meth.startswith("method") else '"' + meth):
                name = name.split("(")[0]
                if name.startswith("L") and ";" in name: name = name.split(";")[-1]  # "Lowner;method" form
                wanted.add(name)
    for name in re.findall(r"@Accessor\s*\(\s*(?:value\s*=\s*)?\"(\w+)\"", src): wanted.add(name)
    for name in re.findall(r"@Invoker\s*\(\s*(?:value\s*=\s*)?\"([\w<>]+)\"", src): wanted.add(name)
    for name in re.findall(r"@Shadow[^;{]*?\s(\w+)\s*(?:\(|;)", src, re.S): wanted.add(name)
    for name in re.findall(r"@Overwrite[^{]*?\s(\w+)\s*\(", src, re.S): wanted.add(name)
    for t in resolved:
        out = members(t)
        if out is None: print(f"{n}: target class not found: {t}"); problems += 1; continue
        for w in sorted(wanted):
            if w in ("<init>", "<clinit>"):
                if w == "<init>" and "(" in out: continue
            if not re.search(r"[\s.]" + re.escape(w) + r"[(;]", out) and not (w == "<init>"):
                print(f"{n}: {t} has no member '{w}'"); problems += 1
print("problems:", problems)

# Second pass: every @At(target = "Lowner;name(desc)ret") / "Lowner;field:type" must exist with that exact descriptor.
sigs = {}
def descriptors(cls):
    if cls not in sigs:
        r = subprocess.run([javap, "-p", "-s", "-cp", cp, cls], capture_output=True, text=True)
        found = set()
        if not r.returncode:
            lines = r.stdout.splitlines()
            for i, line in enumerate(lines):
                if line.strip().startswith("descriptor:"):
                    decl = lines[i - 1]
                    nm = re.search(r"([\w$<>]+)\s*\(", decl)
                    if nm is None: nm = re.search(r"([\w$]+);\s*$", decl)
                    if nm is None and decl.strip().startswith(cls + "("): nm = None
                    name = "<init>" if re.search(re.escape(cls.split(".")[-1]) + r"\(", decl) and nm and nm.group(1) == cls.split(".")[-1] else (nm.group(1) if nm else None)
                    if re.search(r"\b" + re.escape(cls) + r"\(", decl): name = "<init>"
                    if name: found.add(name + line.strip()[len("descriptor:"):].strip())
        sigs[cls] = found if not r.returncode else None
    return sigs[cls]
bad = 0
for n in names:
    path = os.path.join("src/main/java", *(pkg + "." + n).split(".")) + ".java"
    if not os.path.exists(path): continue
    src = open(path, encoding="utf-8").read()
    for t in re.findall(r'target\s*=\s*"(L[^"]+)"', src):
        m = re.match(r"L([\w/$]+);([\w$<>]+)(\(.*|:.*)$", t)
        if not m: continue
        owner, name, desc = m.group(1).replace("/", "."), m.group(2), m.group(3)
        if desc.startswith(":"): desc = desc[1:]
        ds = descriptors(owner)
        if ds is None: print(f"{n}: at-target owner not found: {owner}"); bad += 1; continue
        if name + desc not in ds: print(f"{n}: no {owner}.{name}{desc}"); bad += 1
print("at-target problems:", bad)
