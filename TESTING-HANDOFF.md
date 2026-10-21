# Destroy 1.21.1 port — in-game test hand-off

For whoever tests the port in a real game (Azzie or Maddie), and for the agent session that picks up the
results. The port's state is in `PORTING-HANDOFF-3.md` (newest), then `PORTING-HANDOFF.md`. **Pull first.**

The goal is to confirm the port **plays like 1.20.1 Destroy**. Anything that behaves differently from 1.20.1 is a bug
unless it is listed under "Behaviour differences" in `PORTING-HANDOFF.md`.

## What has been checked already (2026-10-21, commit `f9b954de`)

- Compiles with 0 errors; `./gradlew build` builds `dist/destroy-1.21.1-0.2.0.jar`.
- GameTest server: all recipes, tags, loot tables and data maps load with no errors; the GameTest passes.
- Headless dev client (no real player input): joins a world, JEI starts with no broken recipes or Destroy
  errors, every Destroy JEI mixin applies, the player stays connected. Three bugs found and fixed there.
- **Not checked yet:** anything that needs a person to click, look or play. That is this test.

## 1. Setup

Use a **fresh instance** (Prism Launcher or similar) with only these mods first, so a problem can't come from
the rest of the pack:

| Mod | Version |
|---|---|
| NeoForge | 21.1.262 (or newer 21.1.x) |
| Create | 6.0.10 (it bundles Flywheel, Ponder and Registrate) |
| Petrolpark library (`petrolpark`) | 1.21.1-1.5.11 (anything in 1.5.6–1.5.x is accepted) |
| JEI | 19.56.0.441 (or a nearby 19.x) |
| Destroy | `dist/destroy-1.21.1-0.2.0.jar` from this branch |
| Petrolpark Quality (`pquality`) | optional — 1.21.1-0.3.2; only changes seismometer accuracy |

If the game shows a "missing or unsupported mods" screen, screenshot it and send it — that list is the fix.

Give the instance at least 4 GB RAM. Make a **new Creative world** (cheats on). After the core tests pass,
repeat a quick check with the full Maddiecraft pack (section 4).

## 2. Test checklist

Tick what works, note what doesn't. "Expected" is how 1.20.1 behaved.

### Start-up and world

- [ ] Game reaches the title screen; world creates and loads; you are not kicked on join.
- [ ] Creative tab(s) for Destroy show items with textures (no purple/black squares) and names (no
  `item.destroy.something` keys).
- [ ] Place a few Destroy blocks (vat controller, centrifuge, dynamo, distillation tower parts, aging barrel):
  correct models, they break and drop themselves.
- [ ] Leave and rejoin the world: blocks and their contents are still there.

### JEI (step 6 — the newest work)

- [ ] Press R / U on Destroy items: their recipe categories open and look right (icons, slots, arrows,
  animations). Worth opening: Centrifugation, Distillation, Aging, Electrolysis, Arc Furnace (+ Blasting and
  Smelting variants), Charging, Extrusion, Mutation, Tapping, Cartography Table, Vat Materials, Mixable
  Explosives, Flame Retardant application, Reactions and Generic Reactions.
- [ ] The **Blacklight** sequenced assembly shows its charging step with a dynamo (was broken, now fixed).
- [ ] Hover a **mixture fluid** in a recipe: tooltip names the mixture and lists what's in it.
- [ ] Molecules show up in the JEI ingredient list; R / U on a molecule lists reactions and machine recipes
  that use / make it. Same on a mixture fluid ingredient.
- [ ] Create's Mixing, Packing, Spout Filling, Draining and Deploying categories include Destroy's recipes
  (Deploying: disc stamping; Mixing: potion fluid mixing if brewing in the mixer is enabled).
- [ ] Redstone Programmer screen: drag an item from JEI onto a slot (ghost ingredients).
- [ ] JEI doesn't cover the extra inventory slots (extended inventory) when they're visible.

### Extended inventory

- [ ] Extra hotbar / inventory slots appear when the feature gives them; items can be moved in and out.
- [ ] The **selected extra hotbar slot** has the normal white selection frame (was a black square, now fixed).
- [ ] Items in extra slots survive leaving and rejoining.

### Chemistry and processing

- [ ] A vat works: fill it, heat it, a reaction runs, contents show in the vat's tooltip / goggles.
- [ ] Centrifuge, distillation tower, aging barrel, dynamo (electrolysis and charging), extrusion die: each
  runs one recipe from JEI and produces the shown output.
- [ ] Measuring cylinder / test tubes: pick up and transfer mixtures.
- [ ] Hazmat suit / gas mask: wearable, repairable with the item JEI shows.

### Pollution (step 3 — reworked: 1.20.1 behaviour + the author's new acid rain)

- [ ] Vent a polluting fluid (or `/pollution set …`): the pollution levels change and show in the
  pollution UI / goggles.
- [ ] Smog tints grass and water locally; high pollution has its effects (as 1.20.1).
- [ ] Rain while acidic slowly changes blocks (mossy → plain, bricks → cracked, dirt → coarse dirt) and
  oxidizes copper — **new, approved**.
- [ ] Bone meal on a polluted chunk sometimes **fails: the bone meal is used up and nothing grows**
  (changed on request; 1.20.1 only showed particles).
- [ ] Radioactive fluids pollute their chunk (radioactivity is back, as 1.20.1).

### Seismology (step 3 — 1.20.1 + the author's extras)

- [ ] Seismometer: use it, it finds crude oil (default provider Crude Oil), messages appear, XP is given.
- [ ] Seismograph: mark guesses, the map fills in; the Fill / Complete advancements can trigger.
- [ ] Drilling into a found oil chunk gives crude oil.

### Other features worth a look

- [ ] Swiss Army Knife: switches tool by what you look at (this caused the join kick — now fixed).
- [ ] Glassblowing with the blowpipe; keypunch / circuit boards (trypolithography); redstone programmer
  recording and playback; music disc stamping.
- [ ] Explosives: mix a custom explosive and set it off.
- [ ] Ponder scenes for a couple of Destroy blocks open and play.

## 3. What to send back

For anything that's wrong:

1. **What you did → what you expected → what happened** (one line is enough), plus a screenshot.
2. `logs/latest.log` from the instance folder — **also when nothing crashed**; warnings show up there.
3. For a crash: the newest file in `crash-reports/`.
4. If it's about a specific block or item, its name (or the JEI recipe you were looking at).

Put them in the repo under `test-reports/<date>/` (commit and push), or send them to the agent session
directly. Logs can contain your username and file paths; that's all fine for this repo, but don't add server
addresses or keys.

**Safe to ignore in the log** (already known, not Destroy bugs): mixin warnings about Farmer's Delight, Jade,
Embeddium or Sodium classes not found (optional compat), `petrolpark:item/order` missing model and
`chain_conveyor_hook` missing textures (Petrolpark library), missing JEI config translations, `libflite`
narrator errors on Linux.

## 4. Full pack and dedicated server (after section 2 passes)

- Drop the jar into the full Maddiecraft instance and load a world: same quick start-up + JEI check.
- Dedicated server (steps 8/9 in `PORTING-HANDOFF.md`; server box runbook there): server starts, a client
  joins, a vat reaction and pollution sync to the client.

## 5. For the agent picking up the results

- Reproduce in the dev client where possible (`PORTING-HANDOFF-3.md` → "Client smoke-test runbook"; headless
  under Xvfb, screenshots with ImageMagick `import`).
- Compare against 1.20.1 (`git show origin/1.20.1:<path>`, raw JEI sources in `build/jei-1201/`) before changing
  behaviour; fix toward 1.20.1. Unavoidable differences → "Behaviour differences" in `PORTING-HANDOFF.md`.
- Rebuild `dist/`, push, update the hand-off docs, and tick what was confirmed in this file.
