# Destroy → NeoForge 1.21.1 port — hand-off #5 (pack rebuild + Maddiecraft server setup)

**Read order:** `PORTING-HANDOFF.md` (steps 1–4, build, environment, API mappings, runbooks),
`PORTING-HANDOFF-2.md`, `PORTING-HANDOFF-3.md` (step 6 JEI: DONE, client smoke-tested),
`TESTING-HANDOFF.md` (in-game test plan + autotest side-by-side results), then this.
The port compiles with 0 errors and the jar is built; this session rebuilt the pack files
and set up the server box. Two humans: Azzie (Linux, this machine) and Maddie (Windows,
git identity `Meowfoe`). **Pull before starting.**

## Are the mod files public? (asked 2026-10-21)

**Yes.** `maddlemon/Destroy` is a **PUBLIC** repo (`gh repo view`: `visibility: PUBLIC`).
The built mod jar is committed at `dist/destroy-1.21.1-0.2.0.jar` on branch `1.21.1-neoforge`
and is publicly downloadable from the raw tree URL (verified 2026-10-21, sha1 matches the
local build of `010dd895`):

```
https://raw.githubusercontent.com/maddlemon/Destroy/1.21.1-neoforge/dist/destroy-1.21.1-0.2.0.jar
sha1 cb5e77293708a3b3cce0d0aca5be71737df3b1a5 (9398856 bytes at 010dd895)
```

There are **no GitHub Releases** — the pack index references the raw tree URL instead.
The repo has no LICENSE file (treated as All Rights Reserved in the audit DB). All other
pack mods are public on Modrinth/CurseForge. The pack index embeds sha1+sha512 per file,
so the launcher verifies downloads.

## Pack files (built + verified 2026-10-21)

Location: `/var/home/azziee/Documents/Azziecraft/` — `Maddiecraft-1.21.1-neoforge.mrpack`
(19 KB) + `Maddiecraft-1.21.1-neoforge-prism.zip` (19 KB), both **listing-only** (no jars
inside; launchers download on import). `modlist-1.21.1-neoforge-short.md` updated.

Pipeline (all in `/var/home/azziee/Documents/Azziecraft/build/`): `full_audit.py` →
`full_audit.json` (audit DB) → `download_jars.py` (→ `jars/`, sha1-verified) →
`depcheck_v2.py` → `build_packs.py` → `final_verify2.py`.

What changed in the DB this session:

- **Added `destroy`** (external; GitHub; URL = the raw `dist/` jar URL above; hashes from
  the downloaded bytes; deps create `[6.0.10,6.0.11)` + petrolpark `[1.5.6,1.6.0)`;
  curios/jei optional).
- **Added `petrolpark`** (Petrolpark's Library `1.21.1-1.5.11`; Modrinth maven file
  `https://api.modrinth.com/maven/maven/modrinth/petrolpark/1.21.1-1.5.11/petrolpark-1.21.1-1.5.11.jar`;
  sha1 `967edc04590d24232bfe45faa8c3ee66bface203`; Modrinth project id `ik2WZkTZ`).
  This is the library file, not a normal Modrinth version — the project's newest *version*
  is 1.21.1-1.5.12 (2026-10-19), but the port builds against 1.5.11
  (`gradle.properties: petrolpark_library_version = 1.21.1-1.5.11`), so the pack pins 1.5.11.
- **Re-pinned `jei` 19.64.0.455 → 19.56.0.441 (IMPORTANT).** Petrolpark's Library 1.5.11
  declares an **optional** dependency `jei [19.53.0.426,19.57.0.0)` (verified in the jar's
  `neoforge.mods.toml`), and the Destroy port builds/smoke-tests against
  `jei_version = 19.56.0.441` (Maddie's bump). The audit had pinned 19.64.0.455 (newest),
  which is OUTSIDE the library's supported range — the library's and Destroy's JEI compat
  mixins gate on JEI being loaded, so an out-of-range JEI risks broken JEI integration or
  mixin failures. The pack must stay ≤ 19.57.0.0 until the library/Destroy JEI compat is
  re-tested against a newer JEI. Modrinth newest is 19.69.0.457 — do NOT bump blindly.
  New JEI entry: version_id `ZWGz5dZX`, sha1 `e520422338d3eba053189d042fc08eb40eb22bb8`,
  sha512 `cee8f743819fdeb99fde9a91cd72b9200c07a7fd7e581e3b2561843fd0ee59f0b7dc7e0b619c79cee80402c7557eb4ae3b218bd05ec3e6d9e79801f01b791fa3`,
  size 2156268, type beta (as Modrinth reports it).
- Result: **120 index files (117 Modrinth + 2 external + 1 resource pack)**, 119 jars
  downloaded (421 MB, 0 fails), dep closure **ALL REQUIRED DEPENDENCIES SATISFIED**
  (119 jars, 197 mod ids, 259 required edges). `final_verify2.py`: index valid, prism
  structure correct, doc ↔ index **mismatches: none**.
- `final_verify2.py` section 3 reports 8 "FAIL"s = newer-version-available notices, all
  intentional pins or benign: `jei` (intentional, see above), `petrolpark` (intentional
  1.5.11), and 6 content mods where a newer build exists but the tested pins are kept:
  ambientsounds 6.3.10, puzzles-lib 21.1.64, sophisticated-backpacks 1.21.1-3.26.12.2208,
  sophisticated-core 1.21.1-1.5.14.2413, amendments 1.21-2.1.11, fresh-animations 1.10.4.
  (The user's standing "bump to newest" preference is overridden here by tested-pin safety;
  offer the list to the user before bumping.)

## Server box (Maddiecraft) — setup done, first boot crashed on port, root-caused

Box: `azziee@152.53.94.90`, **sshd on port 22** (17171 refused), Java 21.0.12.1, 8 cores,
15 GB RAM, ~340 GB free. No screen/tmux (use `setsid nohup`; sudo is passwordless but no
dnf/apt found). Key: `/var/home/azziee/Documents/Azziecraft/netbox` (passphrase-protected) →
copy to `~/.ssh/netbox`, `chmod 600`, then **`ssh -i ~/.ssh/netbox -o IdentitiesOnly=yes -p 22 …`**
(`IdentitiesOnly` alone is not enough — the agent holds several keys and the box rejects
the wrong ones; `-i` selects the netbox key). Passphrase via `SSH_ASKPASS` script +
`SSH_ASKPASS_REQUIRE=force` if the agent is empty. Sessions get killed → `~/.ssh/netbox`
and `/tmp` vanish; re-copy and re-`ssh-add` (or use the askpass env inline).

Done on the box (`~/maddiecraft/`):

- NeoForge **21.1.262** server installed (`neoforge-21.1.262-installer.jar --installServer .`;
  sha1 `79f4b7ac67356de27d411adf47f11e55b4bfcd89`; server jar at
  `libraries/net/neoforged/neoforge/21.1.262/neoforge-21.1.262-server.jar`).
- **96 server-side mods** uploaded to `mods/` (audit entries with `server_side` in
  `required`/`optional`; 24 client-only mods excluded — list in `build/server_mods.txt`).
- `eula.txt` (`eula=true`), `server.properties` (view-distance=10, simulation-distance=8,
  motd=Maddiecraft), `user_jvm_args.txt` (**8G heap + Aikar flags**), `start.sh`
  (`exec java @user_jvm_args.txt @libraries/net/neoforged/neoforge/21.1.262/unix_args.txt nogui`).
- **Stopped an old experiment server**: pid 965, cwd `~/mc`, NeoForge **21.1.248**, running
  4+ days, holding port 25565. SIGTERM → clean stop in ~2 s, port freed. (It was one of the
  "old experiments" per the runbook; it can be restarted from `~/mc` if ever needed.)
- First boot of the new server **crashed: "FAILED TO BIND TO PORT! Address already in use"**
  (the old server) → crash report `crash-reports/crash-2026-10-21_19.26.39-server.txt`.
  Resolved by stopping the old server (above). The crash left a partial `world/`
  (`level.dat`, `datapacks/`, `playerdata/`) — **wipe `world/` and `dynamic-data-pack-cache/`
  before the next boot**.

### Found by the real server boot: 2 broken Destroy recipe JSONs (fixed, committed)

The dev/GameTest runtime has Farmer's Delight as **compileOnly**, so FD-gated data is NOT
covered by `runGameTestServer` — the real server (FD present) caught these:

- `recipe/compat/farmersdelight/cutting/onion.json` — FD 1.21.1's `CuttingBoardRecipe.result`
  is a list of `ChanceResult` (`{"item": <ItemStack object {"id": …}>, "chance": …}`; verified
  in FD 1.21.1's `ChanceResult`/`CuttingBoardRecipe$Serializer` and FD's 1.21 source). The
  1.20.1 form `{"item": "minecraft:air"}` no longer parses. Fixed to
  `"result": [{"item": {"id": "minecraft:air"}}]` (air result = the crying effect handled by
  `CuttingBoardMixin`, unchanged).
- `recipe/compat/farmersdelight/cooking/bangers_and_mash.json` — FD 1.21.1 `CookingPotRecipe`
  `container` is an `ItemStack` object: `{"item": "minecraft:bowl"}` → `{"id": "minecraft:bowl"}`.
  (`beetroot_soup.json` was already correct.)
- Scanned **all 401 Destroy recipe JSONs** for 1.20.1 leftovers (result/container
  `{"item":…}`, camelCase `processingTime`/`heatRequirement`/`keepHeldItem`/
  `transitionalItem`/`firstTimeLucky`, old fluid-ingredient forms) — clean otherwise.

### Known benign third-party data skips in the server boot log (do not chase unless asked)

- `farmersrespite:brewing/melon_juice_from_water` and `farmersdelight:melon_juice`:
  condition parse error mentioning `neoforge:never` ("Unknown registry key …
  neoforge:condition_codecs … Input does not contain a key [type]: MapLike[{}]"). Note FD's
  own shipped `data/farmersdelight/recipe/melon_juice.json` (SINGULAR `recipe/` dir in 1.21!)
  has no conditions, so the failing file is likely another mod's datapack recipe at the
  same id — worth one look if melon juice matters. `neoforge:mod_loaded` (used by Destroy's
  FD-compat recipes) parses fine.
- `createdeco:placard`, `endersdelight:chorus_pie_slice`: 1.20-style recipe results inside
  those mods' own data (skipped recipes).
- Railways / Create: Connected loot tables referencing items from absent compat mods
  (byg/tfc/blue_skies/natures_spirit/twilightforest) — skipped loot tables, consistent.
- Moonlight "Fabric API detected!" (Sinytra Connector + Forgified Fabric API — expected,
  proceeds at own risk), "Couldn't load fabric:overlays metadata" (connector, benign),
  journeymap service lines, mixin "Error loading class" WARNs for client-only mods'
  client classes (normal on a dedicated server).

## Do next (resume here)

1. **Rebuild the jar with the recipe fixes** (the fixes are committed in the repo; the jar
   in `dist/` and on the box predates them):
   `cd /var/home/azziee/Documents/Azziecraft/build/destroy && JAVA_HOME=/var/home/azziee/Documents/Azziecraft/build/jdk21 ./gradlew build`
   → new `dist/destroy-1.21.1-0.2.0.jar`.
2. **Update the audit DB `destroy` entry** (new size/sha1/sha512 — hash the freshly built jar;
   the URL/ref stays the same), re-run `python3 build/download_jars.py` (refreshes the jar),
   `python3 build/depcheck_v2.py`, `python3 build/build_packs.py`,
   `python3 build/final_verify2.py`, and update the Destroy row's sha1 note in
   `modlist-1.21.1-neoforge-short.md` (the `cb5e7729…` note + the change-log entry).
   Then commit the pack-side changes if anything is tracked (the pack files themselves live
   outside the repo, in `/var/home/azziee/Documents/Azziecraft/`).
3. **Upload the new jar to the box**: `scp -i ~/.ssh/netbox -o IdentitiesOnly=yes -P 22
   <new jar> azziee@152.53.94.90:~/maddiecraft/mods/destroy-1.21.1-0.2.0.jar` (replaces the old one).
4. **Wipe the partial world**: `ssh … 'rm -rf ~/maddiecraft/world ~/maddiecraft/dynamic-data-pack-cache'`.
5. **Start the server detached**:
   `ssh … 'cd ~/maddiecraft && setsid nohup ./start.sh > console.log 2>&1 < /dev/null & echo launched'`
   — the harness will wait on this ssh session until timeout; that's expected, the server
   survives. Poll separately: `tail console.log`, wait for `Done (…s)! For help, type "help"`,
   then `ss -tln | grep 25565`.
6. **Report to the user**: server up on 25565 (152.53.94.90), pack files rebuilt
   (mrpack + prism zip + doc), Destroy jar public URL, the JEI re-pin rationale, the two
   recipe fixes, and the newer-version-available list (offer to bump).
7. Optional later: investigate the `neoforge:never` condition skips (melon juice); create a
   GitHub **Release** for the Destroy jar so the pack can reference a release asset URL
   instead of the raw tree URL (the runbook mentions "release asset URL"); add FD to the dev
   runtime (or a GameTest with FD present) so FD-gated data is covered by the data check.

## Process

- Compile/build: `cd /var/home/azziee/Documents/Azziecraft/build/destroy && JAVA_HOME=/var/home/azziee/Documents/Azziecraft/build/jdk21 ./gradlew <task> --console=plain`.
- Commit format: `git commit -m "<msg>" -m "Generated by Mistral Vibe.\nCo-Authored-By: Mistral Vibe <vibe@mistral.ai>"`. Push: `git push origin 1.21.1-neoforge`.
- **Ask the user to test the compiled mod / pack after every large step.**
- Session kills: `/tmp` wiped; `~/.ssh/netbox` disappears (re-copy from
  `/var/home/azziee/Documents/Azziecraft/netbox`, `chmod 600`; passphrase via
  `SSH_ASKPASS`+`SSH_ASKPASS_REQUIRE=force` or `ssh-add`); replays reset exec bits
  (`chmod -R +x build/jdk21/bin/`, `chmod +x gradlew`); `build/stray/com_src_main_java` is a
  stale duplicate — never restore into `src/`.

## History

| Commit | Contents |
|---|---|
| `010dd895` | (Maddie) Comparison results + 1.20.1 side of the autotest driver |
| `5accc708` | (Maddie) In-game comparison with 1.20.1; fix the regressions it found |
| *(this commit)* | Fix 2 FD-compat recipe JSONs for FD 1.21.1 formats (onion cutting ChanceResult result, bangers_and_mash container) + PORTING-HANDOFF-4.md (pack rebuild: Destroy+petrolpark added, JEI re-pinned 19.56.0.441; Maddiecraft server setup on the box; old experiment server stopped). |
