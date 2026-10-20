# usage: python port-tools/migrate_data.py
# One-shot 1.20.1 -> 1.21.1 data migration for src/main/resources and src/generated/resources (idempotent).
#  - renames data folders to 1.21's singular names (recipes -> recipe, tags/items -> tags/item, ...)
#  - converts recipe JSON to Create 6 / NeoForge 21.1 codecs (see PORTING-HANDOFF.md step 7)
#  - converts advancement and loot table JSON (item stacks, NBT -> components, enchantment predicates)
import json, os, re, shutil

ROOTS = ["src/main/resources/data", "src/generated/resources/data"]
FOLDERS = {"recipes": "recipe", "advancements": "advancement", "loot_tables": "loot_table", "structures": "structure",
           "predicates": "predicate", "item_modifiers": "item_modifier", "functions": "function"}
TAG_FOLDERS = {"items": "item", "blocks": "block", "fluids": "fluid", "entity_types": "entity_type", "game_events": "game_event"}
SUBTYPES = {"mixtureFluidWithMolecule", "mixtureFluidWithIon", "mixtureFluidWithSalt", "mixtureFluidWithTaggedMolecules", "pureMixture", "refrigerantDummy"}
RENAMED_KEYS = {"processingTime": "processing_time", "heatRequirement": "heat_requirement", "transitionalItem": "transitional_item",
                "keepHeldItem": "keep_held_item", "acceptMirrored": "accept_mirrored"}
CONFIG_CONDITIONS = {"enableAlcohol": "destroy:common.enableAlcohol", "enableBabyBlue": "destroy:common.enableBabyBlue"}

def snake(name): return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", name).lower()

def merge_move(src, dst):
    if not os.path.exists(dst): shutil.move(src, dst); return
    if os.path.isfile(src): os.remove(src); return  # the 1.21 file already there wins
    for name in os.listdir(src): merge_move(os.path.join(src, name), os.path.join(dst, name))
    os.rmdir(src)

def rename_folders():
    for root in ROOTS:
        if not os.path.isdir(root): continue
        for ns in os.listdir(root):
            nsdir = os.path.join(root, ns)
            for old, new in FOLDERS.items():
                if os.path.isdir(os.path.join(nsdir, old)): merge_move(os.path.join(nsdir, old), os.path.join(nsdir, new))
            tags = os.path.join(nsdir, "tags")
            for old, new in TAG_FOLDERS.items():
                if os.path.isdir(os.path.join(tags, old)): merge_move(os.path.join(tags, old), os.path.join(tags, new))

def nbt_components(nbt):
    return {"minecraft:custom_data": nbt}

def item_stack(o):
    """1.20.1 {"item", "count", "nbt"} result -> 1.21 ItemStack {"id", "count", "components"}"""
    if isinstance(o, str): return {"id": o}
    if not isinstance(o, dict) or "item" not in o: return o
    out = {"id": o["item"]}
    for k, v in o.items():
        if k == "item": continue
        if k == "nbt": out["components"] = nbt_components(json.loads(v) if isinstance(v, str) else v)
        else: out[k] = v
    return out

def fluid_stack(o):
    out = {"id": o["fluid"]}
    for k, v in o.items():
        if k == "fluid": continue
        if k == "nbt":
            nbt = json.loads(v) if isinstance(v, str) else v
            if isinstance(nbt, dict) and set(nbt) == {"Mixture"}: out["components"] = {"destroy:mixture": nbt["Mixture"]}
            else: out["components"] = nbt_components(nbt)
        else: out[k] = v
    return out

def result(o):
    if isinstance(o, dict) and "fluid" in o: return fluid_stack(o)
    return item_stack(o)

def ingredient(o):
    if isinstance(o, list): return [ingredient(i) for i in o]
    if not isinstance(o, dict): return o
    for sub in SUBTYPES:
        if sub in o:
            out = {"type": "destroy:" + snake(sub)}
            out.update({k: v for k, v in o.items() if k != sub})
            return out
    if "fluid" in o and "type" not in o:
        out = {"type": "neoforge:single", "fluid": o["fluid"]}
        out.update({k: v for k, v in o.items() if k not in ("fluid", "nbt")})
        if "nbt" in o: print("  WARNING fluid ingredient with nbt kept without it:", o)
        return out
    if "fluidTag" in o:
        out = {"type": "neoforge:tag", "tag": o["fluidTag"]}
        out.update({k: v for k, v in o.items() if k != "fluidTag"})
        return out
    if o.get("type") in ("forge:nbt", "neoforge:nbt"):
        return {"type": "neoforge:components", "items": o["item"], "components": nbt_components(json.loads(o["nbt"]) if isinstance(o["nbt"], str) else o["nbt"]), "strict": True}
    return o

def conditions(conds):
    out = []
    for c in conds:
        c = dict(c)
        t = c.get("type", "")
        if t.startswith("forge:"): c["type"] = "neoforge:" + t[len("forge:"):]
        if t == "petrolpark:config_boolean":
            c = {"type": "petrolpark:config_bool", "value": CONFIG_CONDITIONS.get(c.get("value"), "destroy:common." + c.get("value", ""))}
        out.append(c)
    return out

def recipe(d, rid):
    d = {RENAMED_KEYS.get(k, k): v for k, v in d.items()}
    if "conditions" in d: d["neoforge:conditions"] = conditions(d.pop("conditions"))
    for key in ("ingredients",):
        if key in d: d[key] = [ingredient(i) for i in d[key]]
    if "ingredient" in d: d["ingredient"] = ingredient(d["ingredient"])
    if "key" in d and isinstance(d["key"], dict): d["key"] = {k: ingredient(v) for k, v in d["key"].items()}
    if "results" in d: d["results"] = [result(r) for r in d["results"]]
    if "result" in d:
        r = d["result"]
        if isinstance(r, str):
            r = {"id": r}
            if "count" in d: r["count"] = d.pop("count")  # stonecutting
            d["result"] = r
        else: d["result"] = result(r)
    if "transitional_item" in d: d["transitional_item"] = item_stack(d["transitional_item"])
    if "sequence" in d: d["sequence"] = [recipe(step, rid) for step in d["sequence"]]
    if d.pop("lucky_first_time", False): d["first_time_lucky_key"] = rid
    return d

def item_predicate(p):
    if not isinstance(p, dict): return p
    p = dict(p)
    if "tag" in p: p["items"] = "#" + p.pop("tag")
    if "item" in p: p["items"] = [p.pop("item")]
    if "nbt" in p: p.setdefault("predicates", {})["minecraft:custom_data"] = p.pop("nbt")
    if "enchantments" in p:
        ench = p.pop("enchantments")
        p.setdefault("predicates", {})["minecraft:enchantments"] = [{("enchantments" if k == "enchantment" else k): v for k, v in e.items()} for e in ench]
    return p

def walk_predicates(o):
    """Item predicates anywhere in advancement criteria / loot conditions."""
    if isinstance(o, list): return [walk_predicates(i) for i in o]
    if not isinstance(o, dict): return o
    out = {}
    for k, v in o.items():
        if k in ("items",) and isinstance(v, list) and all(isinstance(i, dict) for i in v): out[k] = [item_predicate(walk_predicates(i)) for i in v]
        elif k in ("item", "predicate") and isinstance(v, dict) and ("items" in v or "tag" in v or "nbt" in v or "enchantments" in v or "item" in v): out[k] = item_predicate(walk_predicates(v))
        else: out[k] = walk_predicates(v)
    return out

def advancement(d):
    disp = d.get("display")
    if isinstance(disp, dict) and "icon" in disp: disp["icon"] = item_stack(disp["icon"])
    if "criteria" in d: d["criteria"] = walk_predicates(d["criteria"])
    if "conditions" in d and isinstance(d["conditions"], list): d["neoforge:conditions"] = conditions(d.pop("conditions"))
    return d

def loot(o):
    if isinstance(o, list): return [loot(i) for i in o]
    if not isinstance(o, dict): return o
    o = {k: loot(v) for k, v in o.items()}
    if o.get("function") == "minecraft:set_nbt": o["function"] = "minecraft:set_custom_data"
    if o.get("function") == "minecraft:copy_nbt":
        o["function"] = "minecraft:copy_custom_data"; o["source"] = o.get("source", "block_entity")
    if o.get("condition") == "minecraft:match_tool" and "predicate" in o: o["predicate"] = item_predicate(o["predicate"])
    return o

def convert(path, fn, *args):
    with open(path, encoding="utf-8") as f: text = f.read()
    try: d = json.loads(text)
    except json.JSONDecodeError as e: print("  bad json", path, e); return
    new = fn(d, *args)
    if new != d or json.dumps(new) != json.dumps(json.loads(text)):
        with open(path, "w", encoding="utf-8", newline="\r\n") as f: json.dump(new, f, indent=4, ensure_ascii=False); f.write("\n")

def main():
    rename_folders()
    for root in ROOTS:
        if not os.path.isdir(root): continue
        for ns in os.listdir(root):
            nsdir = os.path.join(root, ns)
            for kind, fn in (("recipe", recipe), ("advancement", advancement), ("loot_table", loot)):
                base = os.path.join(nsdir, kind)
                for dirpath, _, files in os.walk(base):
                    for name in files:
                        if not name.endswith(".json"): continue
                        p = os.path.join(dirpath, name)
                        if kind == "recipe":
                            rid = ns + ":" + os.path.relpath(p, base).replace(os.sep, "/")[:-5]
                            convert(p, recipe, rid)
                        else: convert(p, fn)

if __name__ == "__main__": main()
