# Destroy → NeoForge 1.21.1 port — hand-off #6 (server up on 25566 + final pack rebuild)

**Read order:** `PORTING-HANDOFF.md` (steps 1–4, build, environment, API mappings, runbooks),
`PORTING-HANDOFF-2.md`, `PORTING-HANDOFF-3.md` (step 6 JEI: DONE, client smoke-tested),
`TESTING-HANDOFF.md` (in-game test plan + autotest side-by-side results), `PORTING-HANDOFF-4.md`
(pack rebuild + first server setup + boot-1/boot-2 crash root causes), then this.
**Pull before starting.** Two humans: Azzie (Linux) and Maddie (Windows, git identity `Meowfoe`).

## UPDATE 2026-10-22: client join crash with Destroy — FIXED (`1ee90b8d`)

**Symptom (Maddie/Azzie's client log):** crash in `ClientPacketListener.handleLogin` when joining —
`InvalidMixinException: PRIVATE @Overwrite method sameDestroyTarget in destroy.mixins.json:client.MultiPlayerGameModeMixin
cannot reduce visibility of PUBLIC target method`.

**Root cause:** Destroy's `client.MultiPlayerGameModeMixin` was a private `@Overwrite` of
`MultiPlayerGameMode.sameDestroyTarget`, a temporary fix for NeoForge#143 marked "TODO remove in 1.21".
**Puzzles Lib** (`puzzleslib-v21.1.64`, in the pack) has an access transformer making that method `public`, and
Mixin refuses a private overwrite of a public method. The dev client never had Puzzles Lib, so it didn't show.
Found by downloading all 116 pack jars and scanning their ATs/classes.

**Fix:** mixin removed. NeoForge 21.1's own `sameDestroyTarget` already has the identical logic
(`destroyingItem.shouldCauseBlockBreakReset(mainHandItem)`), so behaviour is unchanged.
**Verified:** dev client + the pack's Puzzles Lib jar: old code reproduces the exact crash; fixed code joins the world,
JEI starts, 0 ERROR/FATAL lines.
**Audit of the same risk:** the pack's 88 access transformers were scanned against every Destroy `@Overwrite`
target that isn't already public (`SequencedAssemblyRecipe.advance`, `FluidTankBlockEntity.createInventory`,
`ValueSettingsPacket.applySettings`): no pack mod widens any of them. Re-run this scan if mods are added
(any AT that makes an overwritten private/protected method public crashes the same way).

**Also fixed (was in "Known non-fatal items"):** `CartographyTableScreenMixin`'s seismograph error sprite
captured `renderBg` locals (changed in 1.21.1 → injection failed silently). Now drawn at the head of
`renderResultingMap` (same Map data, same draw order). Plus a `pushPose` that should have been `popPose`
after the seismograph preview.

**Pack:** jar rebuilt into `dist/` (sha1 `6f49d09f0eee450aae7046ad717602f8d2a2b804`, 9393656 bytes; public raw URL
verified serving it), audit `destroy` entry updated, `download_jars.py` / `depcheck_v2.py` (all required deps
satisfied) / `build_packs.py` / `final_verify2.py` (index valid, doc ↔ index: no mismatches) re-run;
`Maddiecraft-1.21.1-neoforge.mrpack` + prism zip + `modlist-1.21.1-neoforge-short.md` updated.
`final_verify2` section 3 lists 10 mods with **newer** Modrinth releases than pinned (ambientsounds, jei — pinned
on purpose —, puzzles-lib, sophisticated-backpacks/core, sound-physics-remastered, amendments, naturalist,
fresh-animations, petrolpark) — not errors; not updated (needs the humans' call).

**Server updated (2026-10-21 22:42 box time, user-approved restart):** nobody online; new jar uploaded as `.new`,
server stopped with SIGTERM (last autosave 22:41:09, no players since 22:00), jar swapped
(`~/maddiecraft/mods/destroy-1.21.1-0.2.0.jar` = sha1 `6f49d09f…`; old jar kept at
`~/destroy-1.21.1-0.2.0.jar.old-614cfe42`), restarted with `setsid nohup ./start.sh`: **`Done (2.624s)!`, 0 ERROR lines,
new pid 945822**, TCP 25566 + voice UDP 24455 listening. Old `~/mc` server (25565 / 24454) untouched.
Note: SIGTERM shutdown lines don't reach `latest.log` (log4j closes first) — that's normal here.

**Client update:** re-import the new `.mrpack` (or replace `mods/destroy-1.21.1-0.2.0.jar` with the new `dist/` jar —
the old pack file's hash check will reject the new jar from the URL, so use the new pack file).

## Current state (2026-10-21 ~21:50 UTC): EVERYTHING IS UP

- **Maddiecraft server RUNNING**: `152.53.94.90:25566` (NeoForge 21.1.262, **86 mods**,
  `Done (15.192s)!` at 21:46:08 after the Tectonic removal + world wipe, pid 940329,
  voice chat UDP **24455**). Old experiment server (`~/mc`, NeoForge 21.1.248, pid 926555)
  untouched on **25565** + voice UDP 24454 — it has a live Discord bridge, **do not kill it**.
- **Pack files rebuilt + verified**: `Maddiecraft-1.21.1-neoforge.mrpack` +
  `Maddiecraft-1.21.1-neoforge-prism.zip` (listing-only, **117 files**: 114 Modrinth +
  2 external + 1 resource pack) + `modlist-1.21.1-neoforge-short.md`, all in
  `/var/home/azziee/Documents/Azziecraft/`. `final_verify2.py`: index valid, prism structure
  correct, doc ↔ index **mismatches: none**. **Tectonic + Lithostitched + Apollib removed**
  (client-crash fix, see below) — 120 → 117 files, server 89 → 86 mods.
- **Destroy jar public**: `dist/destroy-1.21.1-0.2.0.jar` @ `bc4b38e7`, sha1
  **`614cfe428acf25e3648087ee6f9be70d36070a7f`** (9394898 bytes), sha512
  `e322ff6d299dcef52432f37768921f121635c536e9d3ae6a318ae60237eff1042d835349eccff6aaf33547ac300c2fd673f523abcff3238fb90c32fef1105dd5`,
  URL `https://raw.githubusercontent.com/maddlemon/Destroy/1.21.1-neoforge/dist/destroy-1.21.1-0.2.0.jar`
  (re-verified serving 614cfe42 after the push). Repo PUBLIC, no GitHub Releases.
  **Reminder: `dist/` is NOT a gradle output — after `./gradlew build`, copy
  `build/libs/destroy-1.21.1-0.2.0.jar` to `dist/` manually, then commit+push.**

## What this session fixed (after hand-off #5's boot-2 crash)

### 1. Distant Horizons + 6 other client-only mods were wrongly server-side

The audit DB marked 7 mods `server_side: optional/required` that the modlist doc marks side C
(client-only). DH **crashed the dedicated server** during chunk generation
(`ChunkyAccessor.bindOnGenerationProgressEvent → "Chunky is not loaded"` → FATAL). Fixed in
`build/full_audit.json` (`server_side` → `unsupported` for all 7, matching the doc):
**AppleSkin, Distant Horizons, Inventory Essentials, Jade, JEI, JourneyMap, Sound Physics
Remastered**. Server mod list regenerated (`build/server_mods.txt`): **96 → 89 mods** (exactly
the doc's B+S rows; verified 96−7=89 and nothing missing). Both packs now mark those files
`env.server = unsupported`. `depcheck_v2.py`: **ALL REQUIRED DEPENDENCIES SATISFIED**
(259 required edges) — no server-side mod requires any of the 7. The 7 jars were deleted from
the box's `mods/`.

### 2. Onion cutting recipe — three iterations to get it right

`data/destroy/recipe/compat/farmersdelight/cutting/onion.json` (1.20.1 behaviour: cutting an
onion yields nothing, crying effect from `CuttingBoardMixin`):

1. `{"item": {"id": "minecraft:air"}}` — **rejected**: FD 1.21.1 `ItemStack.CODEC` uses
   ITEM_NON_AIR_CODEC ("Item must not be minecraft:air").
2. `{"item": {"id": "minecraft:onion"}, "chance": 0.0}` — **rejected**: vanilla has no onion
   item ("Unknown registry key … minecraft:onion").
3. **FINAL: `{"item": {"id": "farmersdelight:onion"}, "chance": 0.0}`** — loads cleanly
   (confirmed: zero `destroy` recipe errors in the boot-5 console log).

Why chance-0 is correct (verified by `javap -c` on FD 1.21.1-1.3.4, jar in the gradle cache):
`CuttingBoardBlockEntity.processStoredItemUsingTool` returns `recipeOptional.isPresent()` (cut
succeeds whenever a recipe matches + tool valid) and produces items via
`CuttingBoardRecipe.rollResults(RandomSource, fortune, wrapper)` → per-result
`ChanceResult.rollOutput(random, fortune)`; `rollOutput` decrements the count when
`random.nextFloat() > chance + fortuneBonus`, so **chance 0.0 ⇒ count 0 ⇒ ItemStack.EMPTY ⇒
nothing drops** (a plain knife has fortune 0). `assemble()` (`results.getFirst().stack().copy()`)
is NOT on the cutting path (only recipe-book/JEI-style display). The crying effect is
independent: `CuttingBoardMixin` wraps `CuttingBoardBlock.useItemOn →
processStoredItemUsingTool` and applies CRYING + the CUT_ONIONS advancement when the cut
succeeds and the board stack `is(c:crops/onion)` (`farmersdelight:onion` is the tag member).
Side effect: JEI shows the onion as a 0%-chance result — cosmetic only.

All 569 Destroy data JSONs re-validated as parseable JSON after the fix.

### 3. Voice chat port conflict (boot-4 shutdown)

Boot 4 reached `Done` but Simple Voice Chat then failed to bind UDP 24454 (held by the old
`~/mc` server, pid 926555) and **shut the server down** (`[voicechat] Shutting down server`).
Fixed: `~/maddiecraft/config/voicechat/voicechat-server.properties` → `port=24455`
(log: "Voice chat server started at port 24455"). Boot 5: `Done (2.230s)!`, no shutdown.

### 4. Leftover crashed JVM

Boot 2's crashed Maddiecraft JVM (pid 930629) was stuck in "Server already shutting down"
(sleeping, no port held) — `kill -9` before boot 4. Watch for this after any crash.

## Client crash investigation (2026-10-21, Modrinth App on Maddie's Windows box + Prism)

Maddie's Modrinth App instance `Maddiecraft-1.21.1-neoforge (1)` imported the pack fine
(the "Invalid metadata" toast was an older pack build — user confirmed) but **crashed on
startup**. Game log saved at `/var/home/azziee/Documents/mclogmodrinth.txt` (Maddie's machine,
200 KB, ends mid-crash-report — see caveat below).

### Root cause: Tectonic without Lithostitched on the client

```
[21:08:52] [modloading-worker-0/ERROR] [FMLModContainer/LOADING]: Failed to create mod instance.
ModID: tectonic, class dev.worldgen.tectonic.platform.neoforge.TectonicNeoforge
java.lang.NoClassDefFoundError: dev/worldgen/lithostitched/api/registry/LithostitchedBuiltInRegistries
Caused by: java.lang.ClassNotFoundException: dev.worldgen.lithostitched.api.registry.LithostitchedBuiltInRegistries
```

- Tectonic is in the client pack (`client_side: optional`); it touches Lithostitched classes
  in its constructor (`TectonicRegistrations.init`) — but it does **not declare** a dependency
  on `lithostitched` in its `neoforge.mods.toml` (verified), so NeoForge's dependency check
  passed and the mod crashed at construction instead of being blocked.
- Lithostitched is marked **server-only on Modrinth** (project `client_side: unsupported`,
  confirmed via the API), so the audit had `client_side: unsupported` and client installs
  never got it. (Its jar itself is a plain worldgen library — it would run on a client — but
  the pack metadata excluded it.)
- The failed construction put the loader in a broken state — the log is full of
  `Cowardly refusing to send event … to a broken mod state` (SoundEngineLoadEvent,
  RegisterColorHandlers, ModelEvent$RegisterAdditional/ModifyBakingResult, …) — and the
  client crashed during startup at 21:08:56, right after
  `[minecraft/SoundEngine]: Error starting SoundSystem. Turning off sounds`
  (OpenAL device: "OpenAL Soft on Reproduktory (Razer Kraken Kitty V2)"; Sound Physics
  Remastered had just initialized).
- **Caveat:** the console log ends mid-crash-report (cut off inside the System Details mod
  list), so the crash report's Description/stacktrace were NOT captured. The full report is at
  `%APPDATA%\ModrinthApp\profiles\Maddiecraft-1.21.1-neoforge (1)\crash-reports\crash-*.txt`
  on Maddie's machine. The broken mod state is a definite fatal defect; whether the final
  frame (sound init) was a downstream casualty or an independent issue (suspects: Sound
  Physics Remastered vs that USB headset, or a heap OOM with the 6 GB `-Xmx`) is unconfirmed.

### Fix applied (user's call): remove Tectonic + its whole dependency closure

Reverse-dependency scan of **every** jar's `neoforge.mods.toml` (incl. jar-in-jar): nothing
depends on `tectonic` (0), nothing declares `lithostitched` (0 — Tectonic doesn't even declare
it), and only Tectonic + Lithostitched declare `apollib`. So all three were removed:

- `full_audit.json`: 120 → **117 entries**; the 3 jars deleted from `build/jars/`
  (116 jars, 0 download fails); `depcheck_v2.py`: **ALL REQUIRED DEPENDENCIES SATISFIED**
  (254 required edges).
- Packs rebuilt: **117 files (114 Modrinth + 2 external + 1 resource pack)**;
  `final_verify2.py`: index valid, doc ↔ index **mismatches: none**.
- Doc: Tectonic/Lithostitched/Apollib rows removed, the obsolete "install Lithostitched
  manually or disable Tectonic" note replaced, counts updated, change-log bullet added.
- Box: 3 jars removed from `~/maddiecraft/mods/` (**86 mods**), `world/` +
  `dynamic-data-pack-cache/` wiped (the old world had Tectonic terrain), server restarted:
  `Done (15.192s)!` at 21:46:08, **zero** tectonic/lithostitched/apollib mentions in the
  log, no new crash reports. Ports unchanged (25566 + voice 24455; old server on 25565/24454).

### Prism "crash" is a different, environmental issue

The Prism log (Azzie's Linux machine, flatpak) shows the game getting **through mod loading**
into `Minecraft.<init>` → window creation, then:
`ERROR DISPLAY … We seem to be having trouble handing off the window, tried for 1 second`
and `java.awt.HeadlessException` in the crash-report thread. That is a **headless
environment** (no X/Wayland display where Prism was launched), not a pack problem. Launch
Prism from a desktop session with `DISPLAY` set. (The Mojang session-server read timeouts
in the same log are network flakiness — retry.) The `union:` schema WARNs at the top are
normal NeoForge-on-Prism noise.

### Prism "GLX: Failed to find a suitable GLXFBConfig" (2026-10-21, Azzie's machine) — not a pack problem

Every GL version fails in NeoForge's early window (and vanilla's window: "GLX: No GLXFBConfigs returned"),
even with **zero mods**. Cause: Flatpak updated the `org.freedesktop.Platform.GL.nvidia-615-78-08` extension while
Prism was running; the running sandbox kept the old, now-deleted mount (its `GL/nvidia-*/lib` is empty → no
`libGLX_nvidia`). Fix: fully quit Prism (`flatpak kill org.prismlauncher.PrismLauncher`) and start it again.
Check: `flatpak enter <prism pid> ls /usr/lib/x86_64-linux-gnu/GL/nvidia-*/lib | wc -l` (0 = stale).

### Known non-fatal items seen in the client log (do not chase unless asked)

- **Destroy port bug (cosmetic):** `destroy.mixins.json:client.CartographyTableScreenMixin`
  injection error — `LVT in CartographyTableScreen::renderBg … has incompatible changes at
  opcode 89` (1.21.1 changed renderBg's locals). Effect: the seismograph error sprite on the
  cartography table silently doesn't render. Fix = re-capture the `@Inject` locals against
  1.21.1's `renderBg` LVT (decompile `CartographyTableScreen` from the client jar). Not a
  crash; follow-up item.
- Estrogen 6.0.10 / Create: Estrogen: ~23 `Unable to load model: 'estrogen:item/…'
  FileNotFoundException` WARNs although the model files ARE in the jar, and the jar has
  **zero** `assets/estrogen/items/*.json` (1.21.1 item-model definitions) — upstream
  packaging quirk; cosmetic missing-model fallback, not fatal.
- EuphoriaPatcher "SHADER NOT FOUND" (no Complementary shaders installed) — expected without
  a shaderpack.
- `Access transformer file META-INF/accesstransformer.cfg provided by mod pride does not
  exist!` — upstream (pride), benign.
- The `Error loading class …` WARNs (mekanism/emi/tfc/IE/jdt/fieldguide/etc.) are mixin
  targets from absent optional-compat mods — normal.

### If the client STILL crashes after the Tectonic removal

Get `crash-reports/crash-*.txt` from the Modrinth App instance folder (path above) and read
the Description + top stack frames. Prime suspects: Sound Physics Remastered vs the Razer
Kraken Kitty V2 OpenAL device (the "Error starting SoundSystem" line immediately precedes the
crash), or a heap OOM (`-Xmx6144M` — the Modrinth App default; bump it in instance settings
if the machine has headroom).

## Boot-5 log status (the good boot)

- `Done (2.230s)! For help, type "help"` (20:38:33); world from boot 4 reused (no wipe needed —
  recipes reload every boot; only wipe `world/` + `dynamic-data-pack-cache/` after a *crash*
  mid-generation).
- Mods confirmed loaded: `Destroy 0.2.0 (destroy)`, `Petrolpark's Library 1.5.11 (petrolpark)`.
- **Zero Destroy recipe/data errors.** Only 4 recipe parse skips remain, all known-benign
  third-party data (do not chase unless asked): `createdeco:placard`,
  `endersdelight:chorus_pie_slice` (1.20-style results in those mods' own data),
  `farmersdelight:melon_juice` + `farmersrespite:brewing/melon_juice_from_water`
  (`neoforge:never` condition parse error — FD's own shipped melon_juice.json has no conditions,
  so the failing file is likely another datapack's recipe at the same id).
- Ports: Maddiecraft TCP 25566 + voice UDP 24455 (pid 933708); old server TCP 25565 + voice
  UDP 24454 (pid 926555). No new crash reports (only boot-1/boot-2 leftovers in
  `crash-reports/`).

## Box runbook (unchanged from hand-off #5, restated)

- SSH: `ssh -i ~/.ssh/netbox -o IdentitiesOnly=yes -p 22 azziee@152.53.94.90` (sshd is port 22,
  NOT 17171; key at `/var/home/azziee/Documents/Azziecraft/netbox`, passphrase via
  `SSH_ASKPASS` script + `SSH_ASKPASS_REQUIRE=force`, or `ssh-add ~/.ssh/netbox`).
- Start detached:
  `ssh … 'cd ~/maddiecraft && setsid nohup ./start.sh > console.log 2>&1 < /dev/null & echo launched'`
  (run via a background process — the harness waits on the ssh call otherwise). Poll
  `~/maddiecraft/console.log` for `Done (…s)!` and `ss -tln | grep 25566`.
- Stop: `kill <pid>` (find with `ps aux | grep 21.1.262`).
- `~/maddiecraft/`: NeoForge 21.1.262, 89 mods, `eula.txt`, `server.properties`
  (view-distance=10, simulation-distance=8, motd=Maddiecraft, **server-port=25566**),
  `user_jvm_args.txt` (8G heap + Aikar flags), `start.sh`,
  `config/voicechat/voicechat-server.properties` (**port=24455** — keep in sync with the
  old server's 24454).
- Sessions get killed → `~/.ssh/netbox` and `/tmp` vanish; re-copy the key, re-set exec bits
  (`chmod -R +x build/jdk21/bin/`, `chmod +x gradlew`).

## Pack pipeline (in `/var/home/azziee/Documents/Azziecraft/build/`)

`full_audit.json` (audit DB, 120 entries) → `download_jars.py` (→ `jars/`, sha1-verified,
119 jars, 0 fails) → `depcheck_v2.py` → `build_packs.py` (→ mrpack + prism zip, listing-only)
→ `final_verify2.py`. `server_mods.txt` = audit entries with `server_side` in
required/optional and `ptype=mod` (regenerate inline — no script writes it).
`build_packs.py` embeds `env.client`/`env.server` from the audit, so audit side fixes
propagate into both packs.

Section-3 "FAIL"s in `final_verify2.py` are newer-version-available notices only (9):
intentional pins `jei` 19.56.0.441 (Petrolpark's Library supports only
`[19.53.0.426,19.57.0.0)` — do NOT bump past 19.57 without re-testing) and `petrolpark`
1.5.11 (port builds against it); benign tested pins with newer builds available:
ambientsounds 6.3.10, puzzles-lib 21.1.64, sophisticated-backpacks 1.21.1-3.26.12.2208,
sophisticated-core 1.21.1-1.5.14.2413, sound-physics-remastered neoforge-1.21.1-1.5.1,
amendments 1.21-2.1.11, fresh-animations 1.10.4. Offer the list to the user before bumping.

## Ask the user to test (per the standing process note)

The server is live — worth an in-game pass before calling it done. **Re-import the pack
first** (Tectonic/Lithostitched/Apollib were removed after the client crash; the Modrinth
App instance needs updating or a fresh import):

1. Join `152.53.94.90:25566` with the pack (Modrinth app / Prism import of the listing files).
   Prism: launch from a desktop session with a display (headless → the window-handoff crash).
2. **Startup check**: the client should reach the title screen with no "Failed to create mod
   instance" / "broken mod state" lines. If it still crashes, send the crash report
   (`crash-reports/crash-*.txt` in the instance folder) — see the investigation section.
3. **Onion test**: place an onion on a Farmer's Delight cutting board, cut it with a knife →
   expect the CRYING effect on nearby players + the "Cut Onions" advancement, and **no onion
   item popping out** (that's the chance-0 placeholder doing its job). Report any console spam.
4. General smoke: Create 6.0.10 contraptions, Petrolpark's Library items, JEI 19.56.0.441
   compat (Destroy/Petrolpark JEI plugins), Biomes O' Plenty worldgen, Create: Estrogen.

## Machine caveat

This session ran on a *different* computer whose `build/` was a stale Oct-19 snapshot (no
`jars/`, old audit). Rebuilt everything locally: audit 120 entries (incl. `destroy`,
`petrolpark`, JEI re-pin 19.56.0.441), 119 jars re-downloaded sha1-verified. If resuming on
yet another machine, re-verify those two counts before trusting anything.

## Process

- Build: `cd /var/home/azziee/Documents/Azziecraft/build/destroy && JAVA_HOME=/var/home/azziee/Documents/Azziecraft/build/jdk21 ./gradlew build --console=plain`
  → `cp build/libs/destroy-1.21.1-0.2.0.jar dist/` → commit both → `git push origin 1.21.1-neoforge`.
- Commit format: `git commit -m "<msg>" -m "Generated by Mistral Vibe.\nCo-Authored-By: Mistral Vibe <vibe@mistral.ai>"`.
- After changing the jar: update the audit `destroy` entry (size/sha1/sha512 from the local
  file), re-run `download_jars.py` (re-fetches + verifies from the public URL),
  `depcheck_v2.py`, `build_packs.py`, `final_verify2.py`, and the Destroy sha1 note +
  change-log bullet in `modlist-1.21.1-neoforge-short.md`. Then scp the jar to
  `~/maddiecraft/mods/` and restart the server.
- `build/stray/com_src_main_java` is a stale duplicate — never restore into `src/`.

## History

| Commit | Contents |
|---|---|
| `2410dee3` | (prev session) Rebuild jar: FD-compat recipe fixes (air onion result, bangers_and_mash container) |
| `c7b573e3` | Onion cutting → chance-0 placeholder (air rejected by FD 1.21.1 ItemStack codec); rollResults/rollOutput path verified via javap; jar rebuilt |
| `bc4b38e7` | Onion placeholder item `minecraft:onion` → `farmersdelight:onion` (vanilla has no onion); jar rebuilt; **server boot clean, recipe loads** |
| *(this commit)* | PORTING-HANDOFF-5.md updated: client crash investigation (Tectonic/Lithostitched root cause), **Tectonic + Lithostitched + Apollib removed** from the pack (117 files, server 86 mods, fresh world, server up), Prism headless diagnosis, non-fatal log items (Destroy CartographyTableScreenMixin LVT, Estrogen model warnings) |
| *(this commit)* | PORTING-HANDOFF-5.md (hand-off #6): server up on 25566 + voice 24455, 7 client-only mods moved off the server (DH crash fix), onion recipe final fix, packs rebuilt+verified |
| `1ee90b8d` | Client join crash fix (Puzzles Lib vs Destroy's private `@Overwrite` of `sameDestroyTarget` — mixin removed, NeoForge has the fix) + cartography seismograph error sprite; jar rebuilt; pack files rebuilt |
| *(this commit)* | Server updated to the fixed jar and restarted (pid 945822, Done, 0 errors) |
