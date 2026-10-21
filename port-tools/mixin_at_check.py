# usage: python port-tools/mixin_at_check.py <classpath jars...>
# Stronger static check than mixin_check.py: for every injector in destroy.mixins.json's mixins, find each target method
# (name or name+descriptor) in the target class bytecode, and for INVOKE / INVOKE_ASSIGN / FIELD / NEW @At targets check
# the call site really exists in that method's body. These are the failures that only show when the target class first
# loads in game ("Critical injection failure ... (0/1) succeeded"). Needs JAVA_HOME = JDK 21 for javap.
import json, os, re, subprocess, sys
jars = sys.argv[1:]
cp = os.pathsep.join(jars)
javap = os.path.join(os.environ["JAVA_HOME"], "bin", "javap")
cfg = json.load(open("src/main/resources/destroy.mixins.json"))
pkg = cfg["package"]
names = cfg.get("mixins", []) + cfg.get("client", []) + cfg.get("server", [])
INJECTORS = "Inject|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyConstant|ModifyReturnValue|WrapOperation|WrapWithCondition|ModifyExpressionValue|WrapMethod|ModifyReceiver"
cache = {}

def bytecode(cls):
    """{(name, descriptor): body text} for a class, or None if not found"""
    if cls in cache: return cache[cls]
    r = subprocess.run([javap, "-c", "-p", "-s", "-cp", cp, cls], capture_output=True, text=True)
    if r.returncode: cache[cls] = None; return None
    methods, cur, body = {}, None, []
    lines = r.stdout.splitlines()
    for i, l in enumerate(lines):
        m = re.match(r"^  (?:[\w$<>\[\]., ?&]+ )?([\w.$<>]+)\(.*\)(?: throws [\w.$, ]+)?;$", l) or re.match(r"^  static \{\};$", l)
        if m:
            if cur: methods[cur] = "\n".join(body)
            name = m.group(1) if m.groups() else "<clinit>"
            if name == cls: name = "<init>"
            desc = lines[i + 1].strip().replace("descriptor: ", "") if i + 1 < len(lines) else ""
            cur, body = (name, desc), []
        elif cur: body.append(l)
    if cur: methods[cur] = "\n".join(body)
    cache[cls] = methods
    return methods

def balanced(src, start):
    depth = 0
    for i in range(start, len(src)):
        if src[i] == "(": depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0: return src[start + 1:i]
    return ""

def to_javap_ref(target):
    """'Lowner;name(desc)ret' -> ('owner.name:(desc)ret' form used in javap comments, kind)"""
    m = re.match(r"L([\w/$]+);([\w$<>]+)(\(.*)", target)
    if m: return m.group(1) + "." + m.group(2) + ":" + m.group(3)
    m = re.match(r"L([\w/$]+);([\w$]+):(.*)", target)
    if m: return m.group(1) + "." + m.group(2) + ":" + m.group(3)
    return None

problems = 0
for n in names:
    path = os.path.join("src/main/java", *(pkg + "." + n).split(".")) + ".java"
    if not os.path.exists(path): continue
    src = open(path, encoding="utf-8").read()
    m = re.search(r"@Mixin\s*\(", src)
    if not m: continue
    arg = balanced(src, m.end() - 1)
    targets = re.findall(r'"([\w./$]+)"', arg) if "targets" in arg else []
    resolved = [t.replace("/", ".") for t in targets]
    for c in re.findall(r"([\w.$]+)\.class", arg):
        outer = c.split(".")[0]
        im = re.search(r"import\s+([\w.]+\." + re.escape(outer) + r")\s*;", src)
        resolved.append((im.group(1) if im else outer) + c[len(outer):].replace(".", "$"))
    for cls in resolved:
        methods = bytecode(cls)
        if methods is None: print(f"{n}: target class {cls} NOT FOUND"); problems += 1; continue
        for am in re.finditer(r"@(" + INJECTORS + r")\s*\(", src):
            ann = balanced(src, am.end() - 1)
            line = src[:am.start()].count("\n") + 1
            mspec = re.search(r"method\s*=\s*(\{[^}]*\}|\"[^\"]*\")", ann)
            if not mspec: continue
            for spec in re.findall(r'"([^"]+)"', mspec.group(1)):
                spec = re.sub(r"^L[\w/$]+;", "", spec)
                mname = spec.split("(")[0]
                mdesc = spec[len(mname):] or None
                cands = [k for k in methods if k[0] == mname and (mdesc is None or k[1] == mdesc)]
                if not cands:
                    print(f"{n}:{line} @{am.group(1)} method {spec} not in {cls}"); problems += 1; continue
                for at in re.finditer(r"@At\s*\(", ann):
                    at_body = balanced(ann, at.end() - 1)
                    value = re.search(r'value\s*=\s*"(\w+)"', at_body) or re.match(r'\s*"(\w+)"', at_body)
                    target = re.search(r'target\s*=\s*"([^"]+)"', at_body)
                    if not value or not target or value.group(1) not in ("INVOKE", "INVOKE_ASSIGN", "FIELD", "NEW"): continue
                    t = target.group(1)
                    if value.group(1) == "NEW":
                        needle = "class " + t.strip("L;").split("(")[0]
                    else:
                        needle = to_javap_ref(t)
                    if needle is None: continue
                    own = cls.replace(".", "/") + "."
                    alt = needle[len(own):] if needle.startswith(own) else None
                    if not any(needle in methods[k] or (alt and (" " + alt) in methods[k]) for k in cands):
                        print(f"{n}:{line} @{am.group(1)} {mname}: @At {value.group(1)} {t} not found"); problems += 1
print(problems, "problem(s)")
