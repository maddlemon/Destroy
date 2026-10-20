# Destroy → NeoForge 1.21.1 port — hand-off

Port of **Destroy** (Create chemistry add-on, upstream: `Petrolpark-Mods/Destroy`)
from **Forge 1.20.1 → NeoForge 1.21.1** (MC 1.21.1, NeoForge 21.1.262, Create
6.0.10-280, Petrolpark's Library 1.21.1-1.5.11) for the **Maddiecraft** modpack. When the
jar builds and works, publish a GitHub release on this fork and add the mod + Petrolpark's
Library to the pack.

- Branch: `1.21.1-neoforge` (base: upstream `1.20.1` @ `851d5d55`; the author's own
  unfinished NeoForge work is upstream branch `1.21.1-neo`, also on this fork's remote)
- Package moved `com.petrolpark.destroy` → `petrolpark.mc.destroy` (author's convention)
- Two people work on this: Azzie (Linux box, paths under `/var/home/azziee/...`) and
  Maddie (Windows). **Pull before starting** — the branch moves.

## Goal and rules

**The port must behave as close to the original 1.20.1 mod as possible, with no regressions**
(Maddie's requirement). So:

- No intentional behaviour changes. If something *has* to differ, add it to
  [Behaviour differences](#behaviour-differences-from-1201) and tell the humans.
- Don't drop features or compat without asking. **JEI compat is required** (it was dropped
  earlier; that's reversed).
- Clear upstream bugs (e.g. a save/load key mismatch) may be fixed — list them below.
- Verify APIs against the real jars (`javap`, see Build) instead of guessing; this port has been
  bitten by silent failures (SRG ATs, wrong mixin package, unregistered registries).

## START HERE — current state and next steps

**State:** compile has **863 javac errors**. Recipes (step 1) and packets (step 2) are done at the
code level — the only errors left in their files depend on other steps (see each step's status).
Most common remaining missing symbols: pollution API (`pollute`, `max`, `CAPABILITY`,
`SyncChunkPollutionS2CPacket`, `LevelPollutionS2CPacket`), `GUESSED_TICK`.

**Do next, in this order** (each is a self-contained batch; compile + commit after each):

1. **Recipes (batch 2 core) — DONE.** Mixture fluid ingredients are NeoForge
   `FluidIngredientType`s (`DestroyFluidIngredientTypes`, ids = old JSON key in snake case, e.g.
   `destroy:mixture_fluid_with_molecule`; codecs bridge to each ingredient's existing
   `readInternal(JsonObject)` so parsing rules are unchanged); `DestroyRecipeTypes` rewritten after
   Create 6 `AllRecipeTypes` (`advanced(...)` = library `AdvancedProcessingRecipe.Serializer`,
   `standard(...)` = Create `StandardProcessingRecipe.Serializer`, `itemApplication(...)`); the 14
   processing recipes extend library `AdvancedProcessingRecipe` (biomes / first-time-lucky built in);
   runtime-built recipes use `AdvancedProcessingRecipe.Builder` / `StandardProcessingRecipe.Builder` /
   `ItemApplicationRecipe.Builder`; potion recipes are per-level (`getRecipes(level)`, cached);
   `RecipeFinder` returns `RecipeHolder`s (unwrap with `.value()`, ids via `.id()`); glassblowing has
   its own serializer for `"shape"`; `FillMixedExplosiveItemRecipe` + fireworks ported;
   `CircuitPatternIngredient` is an `ICustomIngredient` (`DestroyIngredientTypes`).
   Trypolithography: `ManualCircuitBoardRecipe` is a library `ManualOnlyCraftingRecipe` wrapping a vanilla
   `ShapedRecipe` (so the library's JEI "manual crafting" category shows it, as in 1.20.1) whose `#` slots
   are `ExampleMaskIngredient`s; its `matches` still skips the manual-only check like 1.20.1.
   `ExampleMaskIngredient` is an `ICustomIngredient` registered as `destroy:example_circuit_mask`.
   `CircuitDeployerApplicationRecipe.specify(id, inv)` takes the holder id (recipes no longer know their id).
   `CircuitSequencedAssemblyRecipe.Serializer` wraps Create 6's codec + validation as `DataResult` errors.
   `SequencedAssemblyRecipeMixin` overwrites Create 6's private `advance(ResourceLocation, ItemStack, RandomSource)`.
   `DiscStampingRecipe`: `StrictNBTIngredient` → `DataComponentIngredient.of(true, stack)`. Consumers:
   sieve (`FTLRecipesBehaviour`, `RecipeHelper.rollResults(random, …)`), dynamo (`RecipeApplier` with
   `returnProcessingRemainder = false` = 0.5.1 behaviour, `matchStaticFilters(RecipeHolder)`), distillation
   (keeps the `RecipeHolder` for `triggerRecipeCrafted`), blowpipe (`BlowpipeItem.get/putFluidIngredient`:
   `SizedFluidIngredient.FLAT_CODEC` as a JSON string under the same `"RequiredFluid"` key), basin reactions
   (`DestroyFluidIngredientHelper.fromFluidStack`), vat materials (library dropped `SingleBlockIngredient` →
   Destroy's own `core/chemistry/vat/material/SingleBlockIngredient`, type registered in `Destroy.init`).
   **Parse-time gotcha:** 1.21.1 parses recipes *before* binding tags and `Ingredient.getItems()` caches
   forever — never call it while parsing; use `DestroyIngredientHelper.parseTimeItems`.
   Left (blocked on other steps): `DiscStampingRecipe`/`DestroyItems` discs need `BlankRecordItem` off
   `RecordItem` (step 5); `ReactionInBasinRecipe`/`DistillationTower` use `Pollution.getLocalTemperature`
   (step 3); `MechanicalSieveVisual` Flywheel `Context` (step 5); the 7 `compat.jei.*` mixins (step 6 — the
   JEI one uses the retargeted `ProcessingRecipeParamsAccessor`, now `SizedFluidIngredient`).
   Original notes: Create 6 recipe API: `ProcessingRecipe.Factory<P,R>` takes two
   type params (`DestroyRecipeTypes`, `CircuitDeployerApplicationRecipe.Serializer`);
   `ProcessingRecipeBuilder` is abstract (~10 `new ProcessingRecipeBuilder(...)` sites); recipe
   serializers move from JSON/`FriendlyByteBuf` to `MapCodec` + `StreamCodec`; recipes are used
   through `RecipeHolder`; `DestroyRecipeTypes.getType` is ambiguous with `IRecipeTypeInfo.getType`;
   `AdvancedProcessingRecipeSerializer` → library `compat.create.core.data.recipe.AdvancedProcessingRecipe$Serializer`.
   Fluid ingredients → NeoForge `SizedFluidIngredient`/`FluidIngredient`; Destroy's custom fluid
   ingredients (`core/recipe/ingredient/fluid/*`, incl. `MixtureFluidIngredient`, which still uses
   item `getOrCreateTag`) become NeoForge `FluidIngredientType`s. Crafting ingredients
   (`IIngredientSerializer`/`StrictNBTIngredient`/`AbstractIngredient`) → `ICustomIngredient` + `IngredientType`.
2. **Packets (batch 3) — DONE.** The 21 old `S2CPacket`/`C2SPacket` classes are Catnip
   `ClientboundPacketPayload`/`ServerboundPacketPayload`s registered in `DestroyPackets` (enum id = class name
   without the `S2C`/`C2S`+`Packet` suffix, in snake case). Each keeps its 1.20.1 buffer constructor and `toBytes`
   (now on a `RegistryFriendlyByteBuf`), so the encoding is unchanged:
   `STREAM_CODEC = StreamCodec.ofMember(X::toBytes, X::new)`. `handle(LocalPlayer|ServerPlayer)` holds what used to
   be `enqueueWork`ed (Catnip runs handlers on the main thread). `DestroyMessages` is deleted; its 38 send sites
   use `CatnipServices.NETWORK` (`sendToServer`, `sendToClient(player, p)`, `sendToAllClients`,
   `sendToClientsTrackingEntity(e, p)`, `sendToClientsTrackingChunk(level, chunkPos, p)`, and
   `sendToClientsAround(level, blockSource.center(), 32d, p)` for the old `sendToAllClientsNear`).
   `NetworkHooks.openScreen` → `ServerPlayer.openMenu(provider, writer)`; `NetworkHooks.getEntitySpawningPacket` →
   removed (NeoForge sends `IEntityWithComplexSpawn` data itself; spawn data methods take `RegistryFriendlyByteBuf`).
   `ServerGamePacketListenerImplMixin` (creative mode into extended slots) retargeted: 1.21's
   `handleSetCreativeModeSlot` has one `isEmpty()` call (ordinal 0, verified in bytecode; it was ordinal 1) and
   no `CompoundTag` local, so it reads the record packet instead of capturing locals.
   **Left for step 3:** the pollution sends in `DestroyCommonEvents` and `ServerLevelMixin` already use
   `CatnipServices.NETWORK` but still construct the deleted 1.20.1 `LevelPollutionS2CPacket` /
   `SyncChunkPollutionS2CPacket` — switch them to the author's `LevelPollutionPacket` / `ChunkPollutionPacket`.
3. **Pollution / crude oil consumers.** Old code still uses the 1.20.1 `Pollution.CAPABILITY`,
   enum fields `.local`/`.max`, `PollutionType.values()`, `PollutionHelper.pollute(...)` (10 sites),
   `changePollutionGlobal`, `getCropGrowthFailureParticles`, `SyncChunkPollutionS2CPacket`. Port them
   onto the author's design: `DestroyPollutionTypes` registry entries, `LevelPollution`/`ChunkPollution`
   attachments, and data-map `PollutionType.Properties` (max, decay, etc). Same for crude oil: old
   `content.oil.ChunkCrudeOil` (capability) vs the author's `core.oil.ChunkCrudeOil` (attachment) —
   keep the author's. Users: `SmogAffectedBlockColor`, `ServerLevelMixin`, `ClientNeoForgeMod$1Mixin`,
   `ForgeMod`-style water tint, `PumpjackBlockEntity`, seismology (`GUESSED_TICK`, `OilPonderScenes`).
   Then re-enable the pollution ponder storyboards marked `TODO(port)` in `DestroyPonderScenes`
   (the author's `PollutionPonderScenes` is commented out upstream — port it from upstream `1.20.1`).
4. **Item capabilities + leftover NBT.** `ItemMixtureTank`, Blowpipe spout handler, measuring
   cylinder, test tube → `Capabilities.FluidHandler.ITEM` registered with `event.registerItem` in
   `DestroyCapabilities` (handlers backed by `ItemStackNBT` / data components). Inventory
   `serializeNBT()`/`deserializeNBT()` now take a `HolderLookup.Provider`. Potion fluids in
   `CentrifugeBlockEntity` → `DataComponents.POTION_CONTENTS` + Create's bottle-type component.
   `SequencedAssemblyRecipeMixin` must be rewritten against Create 6 (progress is a Create data
   component now, not `"SequencedAssembly"` NBT).
5. **Remaining API changes** (smaller, scattered): MobEffect `applyEffectTick`/attribute-modifier
   signatures; `ToolActions` → `ItemAbilities`; `PlantType` gone; `RecordItem` → jukebox song
   components; Create 6 `DisplaySource` API; `DestroyCreativeModeTabs` (library `CustomTab` changed);
   contamination (library classes gone — reimplement minimally); decay items → library
   `ItemDecay`/`StackableFlashingDecayingItem`; client model code; Create `renderFluidBox` signature;
   `Component.Serializer` JSON → codecs.
6. **JEI compat.** Restore `compat/jei` (45 files) and the 7 `compat.jei.*` mixins from upstream
   `1.20.1` and port to JEI 19 (already a dependency). Needs batch 1 done first. Register the mixins
   in `destroy.mixins.json` under `compat.jei.*` so `DestroyMixinPlugin` gates them on JEI.
7. **Data migration.** MC 1.21 renamed data folders to singular: `data/*/recipes|advancements|loot_tables|structures`
   → `recipe|advancement|loot_table|structure` (also in `src/generated`) — until then **none of it
   loads**. Recipe JSON must match the new codecs: Create 6 style (`"type": "neoforge:single"` fluid
   ingredients, `{"id", "amount"}` fluid results, snake_case keys like `heat_requirement`); mixture
   fluid results' `"nbt": {"Mixture": …}` → `"components": {"destroy:mixture": …}`. Look at Create
   6's own JSON (inside its jar) for reference. Destroy-specific conversions: mixture ingredients
   `{"mixtureFluidWithMolecule": "destroy:mixture", "molecule": …, "amount": n}` →
   `{"type": "destroy:mixture_fluid_with_molecule", "molecule": …, "amount": n}` (same for
   `…WithIon`, `…WithSalt`, `…WithTaggedMolecules`, `pureMixture` → `destroy:pure_mixture`); advanced
   params `processingTime` → `processing_time`, `heatRequirement` → `heat_requirement`, `biomes` stays,
   sieving `"lucky_first_time": true` → `"first_time_lucky_key": "<that recipe's own id>"` (per-recipe
   luck like 1.20.1); glassblowing `"shape"` unchanged. Sequenced assembly: `transitionalItem` →
   `transitional_item`; deployer `keepHeldItem` → `keep_held_item`. Crafting results (incl.
   `circuit_board_manual_crafting`) `{"item": …}` → `{"id": …}`.
8. **Clean compile → datagen → `./gradlew build` → runtime test** in the dev client (`runClient`):
   watch for mixin apply failures, missing registries, recipe parse errors; test with Sodium and
   Farmer's Delight installed too. Also start a **dedicated server** (`runServer`) and join it: clientbound
   packet handlers reference client classes, so make sure nothing client-only gets loaded server-side.
9. **Release + pack integration** (runbooks at the bottom).

## Build

**Windows (Maddie):** JDK 21 is `JAVA_HOME`. From Git Bash in the repo:
`./gradlew compileJava --console=plain > compile.log 2>&1`, then read the `N errors` line or run
`python port-tools/errsummary.py compile.log`. ~1.5 min (NFRT cache shared with other mods).
Repo-local git identity is `Meowfoe`.

**Linux (Azzie):** working dir `/var/home/azziee/Documents/Azziecraft/build/destroy` — do **not**
use `/tmp` for anything that must survive.

```
JDK=/var/home/azziee/Documents/Azziecraft/build/jdk21   # local JDK 21, no system Java
cd /var/home/azziee/Documents/Azziecraft/build/destroy
JAVA_HOME=$JDK ./gradlew compileJava --console=plain    # log to a file, run detached, poll
```

- `~/.gradle/gradle.properties` contains
  `org.gradle.java.installations.paths=/var/home/azziee/Documents/Azziecraft/build/jdk21`.
- First-ever run ≈ 9 min (ModDev compiles MC+NeoForge into
  `build/moddev/artifacts/neoforge-21.1.262-{merged,}.jar`); cached runs are seconds.
- Azzie-only scripts in `/var/home/azziee/Documents/Azziecraft/build/`: `parse_errors.py`,
  `phase_a.py`, `phase_a2.py`, `phase_a3.py`, `manual_edits.py`, `resources_migrate.py`, `phase_b1.py`.

**Both:**
- Inspect APIs: `$JAVA_HOME/bin/javap -p -cp <jar> <class>` against
  `build/moddev/artifacts/neoforge-21.1.262-merged.jar` (MC + NeoForge, ATs applied) or the
  dependency jars in `~/.gradle/caches/modules-2/files-2.1/` (create, petrolpark, pquality,
  registrate, ponder, jei, flywheel, maven.modrinth/*). Sodium's classes are inside a jar-in-jar
  (`META-INF/jarjar/net.caffeinemc.sodium-neoforge-*-mod.jar`).
- `port-tools/at_check.py` validates every AT entry against the merged jar.
- Azzie's box also has decompiled reference sources in `/var/home/azziee/Documents/Azziecraft/build/src-ref/`
  (`mc/` = NeoForge sources jar, `create/`, `petrolpark/` = library 1.21.1-1.5.11, `petrolpark-old/` = library
  1.20.1-1.4.27; decompile with the Vineflower jar in the Gradle cache).
- Jar: `./gradlew build` once compile is clean.

## Environment gotchas (Azzie's Linux box — sessions get killed)

- `/tmp` is wiped on restart (logs, extracted jars gone).
- `~/.ssh/netbox` (copy of the server key) disappears — re-copy from
  `/var/home/azziee/Documents/Azziecraft/netbox` and `chmod 600`.
- Replays can restore stale files and reset exec bits: re-run
  `chmod -R +x /var/home/azziee/Documents/Azziecraft/build/jdk21/bin/` and
  `chmod +x gradlew` (or `git update-index --chmod=+x gradlew`) after a restart.
- `build/stray/com_src_main_java` is a stale duplicate of the old source tree
  moved aside by a replay — do **not** restore it into `src/`.
- Ground-truth jars in `build/`: `neoforge-21.1.262-universal.jar`, `fmlloader.jar`
  (`fmlcore.jar` there is a 404 HTML page — ignore it).

## Patterns established in this port (follow them)

- **Block-entity capabilities:** no `getCapability` overrides. Each BE exposes typed
  `getFluidHandler(Direction side)` / `getItemHandler(Direction side)` (return `null` = none on that
  side) and is registered in `DestroyCapabilities` (mod-bus `RegisterCapabilitiesEvent`). Call
  `invalidateCapabilities()` whenever what those return changes. To *consume* a capability use the
  null-safe `DestroyCapabilities.fluidHandler(be, side)` / `itemHandler(be, side)`.
- **Entity data (old entity capabilities):** NeoForge attachments in `DestroyAttachmentTypes`
  (`AttachmentType.serializable(...)`, same ids as the old capabilities). The data classes implement
  `INBTSerializable<CompoundTag>`; access with `X.of(entity)` → `Optional<X>` (empty for entities the
  old capability wasn't attached to). Death copying stays in the `PlayerEvent.Clone` handler.
- **Mixture fluids:** the mixture lives in data component `DestroyDataComponentTypes.MIXTURE`
  (`CompoundTag`). Use `MixtureFluid.getMixtureTag/hasMixtureTag/addMixtureToFluidStack`; never
  mutate a stored tag — `set` a new one.
- **Item NBT:** `util/ItemStackNBT` maps the old `getOrCreateTag()` keys onto vanilla
  `minecraft:custom_data` (same keys). `read` returns a *copy*; write with `update`/`set`. Use real
  vanilla/Create components where they exist (fireworks, dye colour, potion contents, custom name).
- **Mob effects:** `DestroyMobEffects.X` is a `Holder<MobEffect>`; pass it directly (no `.get()`).
  Compare with `holder.is(DestroyMobEffects.X)`, never `==`.
- **Tick events:** Forge fired Player/Level tick events at START *and* END. Handlers that ignored the
  phase subscribe to both `Pre` and `Post` (see `ChemistryHazardHelper`) to keep the 1.20.1 rate.
- **Compat mixins:** put them in `mixin/compat/<modid>/...`. `DestroyMixinPlugin` (via the library's
  `PetrolparkMixinPlugin`) only applies `compat.<modid>.*` mixins when that mod is loaded. For mods
  that aren't compile dependencies, target by name: `@Mixin(targets = "...", remap = false)`.
- **Access transformers:** Mojang names only; `validateAccessTransformers = true` fails the build on
  a bad entry.
- **Packets:** a Catnip payload class (`ClientboundPacketPayload` / `ServerboundPacketPayload`) with a
  `STREAM_CODEC`, `getTypeProvider()` returning its `DestroyPackets` entry, and `handle(player)`; add the entry to
  `DestroyPackets`. Send with `CatnipServices.NETWORK.*`.
- **Registries:** every `Destroy*` registry class must be registered from the `Destroy` constructor —
  compare with the author's `Destroy.java` on `1.21.1-neo` when adding one.

## Behaviour differences from 1.20.1

Intended to be empty. Current entries:

- **Bug fix:** `ExtendedBasinBehaviour` wrote `"EvaporatedFluid"` but read `"EvaporatedFluidStack"`,
  so a basin's evaporated fluid was lost on reload. Now reads the key it writes.
- **Sodium compat is new** (1.20.1 only had Embeddium compat, and the pack uses Sodium). It redirects
  only Destroy's three smog tint resolvers, where the Embeddium mixin redirects all tints; the visible
  result (smog-tinted grass/foliage/water) is the same.
- Pollution ponder storyboards are disabled until step 3 (temporary, marked `TODO(port)`).
- **Upstream bug, now moot:** `CentrifugationRecipe.setAllowedBiomes` assigned its parameter to itself, so
  biome limits on centrifugation never worked; biomes now come from the library base and work. No
  centrifugation recipe sets biomes, so nothing changes in play.
- Upstream quirk kept: `RefrigerantDummyFluidIngredient` never overrode `getType()`, so it reports itself
  as a molecule-tag ingredient (its special JEI description is never used) — unchanged on purpose.
- Upstream quirk kept: `MechanicalSieveBlockEntity` has `read`/`write` swapped (in-progress sieving is never
  saved). Unchanged on purpose — "fixing" it would crash on load when a saved Item entity is gone.
- Not ported: 1.20.1 `CircuitDeployerApplicationRecipe.Serializer` (its deployed-item check was inverted, but it
  was never registered — `CIRCUIT_DEPLOYING` used the library's generic serializer), so nothing changes.
- `ManualCircuitBoardRecipe.getExampleResult` copies the result before adding the example pattern (1.20.1 wrote
  the pattern onto the recipe's own result stack; `assemble` always overwrote it, so crafting is the same).
- `Tags.Items.MUSIC_DISCS` (`c:music_discs`) replaces `minecraft:music_discs`, which 1.21 removed (dynamo disc
  electroplating + Destroy's discs' tags). NeoForge puts every vanilla disc in it.
- `FillMixedExplosiveItemRecipe.matches` passed `null` registries in 1.20.1; it passes the level's now
  (1.21 needs them to read the Item's explosive inventory). Same result.

## Decisions

- Compat: **JEI — required, to be restored (step 6).** Farmer's Delight cutting-board mixin —
  restored (onion cutting makes nearby entities cry + advancement), ported to FD 1.21's `useItemOn` →
  `processStoredItemUsingTool`. Embeddium `WorldSliceMixin` — restored as in 1.20.1; Sodium
  `LevelSliceMixin` — added for the pack. Kept `compat/pquality` (author kept it). Create Big Cannons
  compat dropped (CBC not in the pack; 5.11.x API incompatible) — ask before restoring. Curios compat
  (1 file, never wired up in 1.20.1 either) dropped.
- Config: old config tree kept (Catnip `ConfigBase`-compatible); author's `DestroyConfigs` = engine
  (`@EventBusSubscriber(bus = Bus.MOD)`); `config/DestroyAllConfigs.java` = thin facade
  (`CLIENT/COMMON/SERVER` + `register(ModLoadingContext, ModContainer)`) so `DestroyAllConfigs.SERVER.x` works.
- `DestroyAdvancementTrigger`: `SimpleAdvancementTrigger` → `PlayerTrigger`
  (`CriteriaTriggers.register(Destroy.asResource(e.triggerId), e.trigger)`).
- `Destroy.java`: ctor `Destroy(IEventBus modEventBus, ModContainer modContainer)`; static tooltip
  block removed (no library tooltip factory); compat init lines removed; registers the author's
  registries (`DestroyAttachmentTypes`, `DestroyDataComponentTypes`, `DestroyNumberProviderTypes`,
  `DestroyPollutionTypes`, `DestroySeismologyProviders`) and `DestroyPackets` in the ctor;
  `runForDist/unsafeCallClient/unsafeRunClient` helpers added.
- Resources (done so far): pack.mcmeta ×3 → 48 + supported_formats; biome_modifier →
  `data/destroy/neoforge/biome_modifier` + `neoforge:add_features`; conditions `forge:`→`neoforge:`;
  generated `data/forge/tags/{blocks,items}` → `data/c/tags/{block,item}`
  (glass→glass_blocks, glass/colorless→glass_blocks/colorless, string→strings);
  `data/forge/tags/fluids` → `data/destroy/tags/fluid`; singularized tag dirs; content seds
  (fluidTag urine/crude_oil/apple_juice→`destroy:`, milk→`c:milk`, chocolate→`c:chocolate`;
  `"forge:X"`→`"c:X"`; `#forge:`→`#c:`). Still to do: step 7.
- Mixin config: JAVA_21; must list exactly the files in `mixin/` (the author's 3 cartography
  mixins were missing and are now listed; `ForgeMod$2$1Mixin` → `ClientNeoForgeMod$1Mixin`).

## History

| Commit | Contents |
|---|---|
| `381a135f` | Phase A: `git mv com/petrolpark/destroy → petrolpark/mc/destroy`, grafted the author's 59 NeoForge files, ordered mechanical rewrite (810 files; `new ResourceLocation(...)` → `parse`/`fromNamespaceAndPath`; slash-form mixin descriptors) |
| `7de3041f` | Scaffolding: library imports, tag namespaces (`c:`), config facade, compat cleanup, resources migration (163 JSON), build files, mixins json |
| `21063ac6` | `gradlew` executable bit |
| `29815ce4` | `DestroyTagDatagen` syntax fix; `-Xmaxerrs 10000` on JavaCompile |
| `f7a0c130` | Phase B batch 1 (mechanical): `DestroyLang` imports, `CatnipServices`, `AllTags` imports, `DistExecutor`→`Destroy.unsafeRunClient`, `RegistryObject`→`DeferredHolder`, nested import fixes, `saturationModifier` |
| `207d3fd1` | AT registration `files()`→`from()`, `BlockSource` package, `Explosion.center()/radius()` |
| `9e90380a` | **AT blocker fixed** (entries were SRG names; NeoForge 1.21 only matches Mojang names) + validated against the jar; Create 6 `ProcessingRecipeParams` import; `HolderLookup.Provider` through BE/behaviour `read`/`write`/`writeSafe`; `FluidStack` NBT → `parseOptional`/`saveOptional` |
| `955c7499` | Tick events → `ClientTickEvent`/`PlayerTickEvent`/`LevelTickEvent`; `DestroyPollutionTypes` refs; pollution ponder storyboards disabled; `ItemProviderEntry<?, ?>` |
| `85efc333` | Mob effects → `Holder<MobEffect>`, `Registries.MOB_EFFECT` |
| `7bfb29aa` | Block-entity capabilities → `DestroyCapabilities`; `BlockSource` record accessors |
| `7bdf6d44` | Entity capabilities → attachments; **registered the author's missing registries** in the ctor |
| `bd702b4b` | Mixture `MIXTURE` component; `ItemStackNBT`; fireworks/dye components; tick handlers back to 2x/tick; `EvaporatedFluid` key fix |
| `244b5b16` | Recipes part 1 (mixture fluid ingredients, recipe types, recipe bases, builders, glassblowing serializer) |
| `62c33e8d` | Recipes part 2 (per-level potion recipes, `RecipeHolder` lookups, `PotionBrewing$Mix` AT) |
| `89cfdbc6` | Fill-explosive + circuit pattern ingredient; registries threaded through mixed-explosive item helpers |
| `127f23a1` | Farmer's Delight / Embeddium / Sodium compat mixins; **`DestroyMixinPlugin` package fixed** (was `com.destroy.mixin`, so compat mixins were never gated — would crash without the mod); water tint mixin retargeted to `ClientNeoForgeMod$1`; missing cartography mixins listed; `port-tools/`; this doc restructured |
| `26b63081` | Recipes part 4: trypolithography + disc stamping recipes, sequenced assembly mixin, recipe consumers (sieve, dynamo, distillation, blowpipe, basin reactions, vat materials) |
| _(this commit)_ | Packets: 21 packets → Catnip payloads in `DestroyPackets`, `DestroyMessages` deleted, 38 send sites → `CatnipServices.NETWORK`, `NetworkHooks` replaced, creative-slot mixin retargeted |

Error counts: 2,301 error lines (batch 1) → 2,178 javac → 2,050 → 1,940 → 1,892 → 1,718 → 1,621 → 1,502 → 1,501 → 1,281 (recipes 1) → 1,217 (recipes 2) → 1,184 → 1,071 (recipes 4) → 863 (packets).

## Verified API mappings (NeoForge 21.1.262 / MC 1.21.1 / Create 6.0.10 / library 1.21.1-1.5.11)

NeoForge: `NeoForge`, `NeoForgeMod`, `ModConfigSpec`, `DeferredHolder<R,T>` (RegistryObject
GONE), `DeferredRegister` (+`.DataComponents`), `Capabilities.{FluidHandler,ItemHandler,EnergyStorage}.{BLOCK,ITEM}`,
`RegisterCapabilitiesEvent`, `BlockCapability/ItemCapability/EntityCapability/LevelCapability`,
`AttachmentType` (`builder`, `serializable`, `copyOnDeath`), `INBTSerializable` methods take a
`HolderLookup.Provider`, `event.tick.{Entity,Level,Player,Server}TickEvent` (Pre/Post nested; the
base classes are abstract and can't be subscribed to), `client.event.ClientTickEvent.Pre/Post`,
`EventHooks` (was ForgeEventFactory), `IShearable`, `IEntityWithComplexSpawn`, extensions
`IItemExtension/IBlockExtension/IBlockStateExtension/IBlockEntityExtension/IEntityExtension/IMenuTypeExtension`,
`IBlockEntityExtension.invalidateCapabilities()` (null-level safe), `RegisterGuiLayersEvent`,
`VanillaGuiLayers` (at `client.gui`), `DeferredSoundType` (at `common.util`), conditions at
`net.neoforged.neoforge.common.conditions` (ids `neoforge:mod_loaded|false|true|not|and|or|tag_empty|item_exists`;
`"conditions"` key still works in recipes), `ModConfigEvent implements IModBusEvent`,
`net.neoforged.fml.ModContainer`, `ModLoadingContext` + `LogicalSide`, `InterModComms`. Fluid client
extensions live in `ClientNeoForgeMod` (water = anonymous `ClientNeoForgeMod$1`). MixinExtras is
available (`@WrapOperation` etc). GONE: ToolActions/ToolAction (→ `net.neoforged.neoforge.common.ItemAbilities`/`ItemAbility`), PlantType,
TierSortingRegistry (exists nowhere in 21.1 — tiers are the `Tool` data component + `incorrect_for_*_tool`
block tags; an earlier note here wrongly said to use a vanilla one), ForgeHooks, ForgeGui,
`registries.tags.ITag`, `Capability/CapabilityManager/CapabilityToken/LazyOptional`, the whole
network stack (SimpleChannel/NetworkRegistry/NetworkHooks/NetworkEvent/NetworkDirection;
PacketDistributor exists), DistExecutor, FMLJavaModLoadingContext, `NeoForgeRegistries.MOB_EFFECTS`
(use `Registries.MOB_EFFECT`).

MC 1.21.1: `FoodProperties.Builder.saturationModifier(float)`; `Explosion.center()` → Vec3;
`CriteriaTriggers.register(ResourceLocation, trigger)`; `ItemStack` `getOrCreateTag/getTag/setTag`
GONE (data components; `CustomData` for free-form NBT); `ItemTags.create/BlockTags.create/FluidTags.create`
GONE (use `TagKey.create(Registries.X, id)`); dispenser `BlockSource` is a record at
`net.minecraft.core.dispenser` (`level()/pos()/state()/blockEntity()`); `CustomRecipe(CraftingBookCategory)`,
`matches/assemble(CraftingInput, …)`, `ShapelessRecipe(group, category, result, ingredients)`, recipe ids
via `RecipeHolder`; `Fireworks`/`FireworkExplosion`/`DyedItemColor` components, `minecraft:dyeable`
tag replaces `DyeableLeatherItem`; item tint colours are ARGB (`FastColor.ARGB32.opaque`);
`Level.createFireworks(..., List<FireworkExplosion>)`; `PotionBrewing` is per-level
(`level.potionBrewing()`); `CompoundTag.CODEC`, `ByteBufCodecs.COMPOUND_TAG`; data folders singular
(`recipe/`, `advancement/`, `loot_table/`, `structure/`, `tags/block|item|fluid`); `pack.mcmeta`
pack_format 48 + `supported_formats: [0, 2147483647]`.

Tags: all conventional under `c:` — `c:strings` (was forge:string), `c:leathers`,
`c:glass_blocks` (+`/colorless`; covers all 16 stained colors via `c:glass_blocks/cheap`),
`c:dusts|ingots|ores|storage_blocks|plates|rods|seeds|raw_materials|nuggets|dyes|gems|crops|tools|buckets`,
`c:foods/raw_meat`, fluid `c:milk`, `c:stones|gravels|cobblestones|sands|netherracks`,
`c:sandstone/blocks` (was forge:sandstone). biome_modifier: `data/<ns>/neoforge/biome_modifier`
+ `"type": "neoforge:add_features"`.

Create 6: `CreateRegistrate`, `TagGen$CreateTagsProvider`, `AllTags.commonItemTag/commonBlockTag/commonFluidTag`
(no `forge*`; `AllTags` has no `tagBlockAndItem`), `SmartBlockEntity`/`BlockEntityBehaviour`
`read/write(CompoundTag, HolderLookup.Provider, boolean)`, `SmartFluidTankBehaviour.getCapability()`
returns the `IFluidHandler` directly, BEs register caps via static `registerCapabilities(RegisterCapabilitiesEvent)`,
`ProcessingRecipeParams` is top-level, `ProcessingRecipe$Factory<P,R>`, `compat.jei`
(`ConversionRecipe`, `MysteriousItemConversionCategory`, `SequencedAssemblySubCategory$AssemblyDeploying`
public no-arg ctor); `IAssemblyRecipe.getJEISubCategory` ABSTRACT → `Supplier<Supplier<SequencedAssemblySubCategory>>`;
`GogglesItem.addIsWearingPredicate(Predicate<Player>)`; `IHaveGoggleInformation.containedFluidTooltip(List, boolean, IFluidHandler)`;
recipe JSON: `"type": "neoforge:single"` fluid ingredients, `{"id","amount"}` results, snake_case keys.

Library (petrolpark 1.21.1-1.5.11): `compat.Mods` enum (`isLoaded()/isLoading()/executeIfInstalled(Supplier<Runnable>)/asResource(String)`);
`compat.curios.{PetrolparkCurios,PetrolparkCuriosSetup}`; `compat.create.core.world.block.entity.behaviour.AbstractRememberPlacerBehaviour`;
`compat.create.core.world.item.transported.{DirectionalTransportedItemStack,IDirectionalBeltItem}`;
`compat.create.PetrolparkCreateClient.OUTLINER`; `core.world.item.creativeModeTab.CustomTab`;
`core.client.ponder.*`, `core.client.rendering.*`, `core.world.item.decay.DecayingItemDecorator`,
`core.world.fluid.ICustomBlockStateFluid`, `util.*`, `core.data.recipe.ingredient.BlockIngredient`,
`core.data.recipe.IBiomeSpecificRecipe`, `core.world.item.crafting.ManualOnlyCraftingRecipe`,
`compat.create.core.data.recipe.firstTimeLucky.{FTLRecipesBehaviour,IFTLProcessingRecipe}`,
`compat.create.core.data.recipe.AdvancedProcessingRecipe$Serializer`,
`core.world.item.decay.{ItemDecay,StackableFlashingDecayingItem,DecayTime}`,
`AbstractPetrolparkRegistrate` (NO setTooltipModifierFactory), `mixin.plugin.PetrolparkMixinPlugin`
(gates `<getMixinPackage()>.compat.<modid>.*` on that mod being loaded).
GONE from library: contamination classes, SimpleAdvancementTrigger, C2SPacket/S2CPacket.
Catnip classes (274, incl. `CatnipServices`, `CatnipPacketRegistry`, `ConfigBase`,
`BasePacketPayload`, `ScreenOpener`, …) are shaded inside Ponder's jar — no Catnip mod/dependency needed.

Compat targets: Farmer's Delight 1.21.1-1.3.4 (`compileOnly`, mod id `farmersdelight`):
`CuttingBoardBlock.useItemOn` → `CuttingBoardBlockEntity.processStoredItemUsingTool(ItemStack, Player)`.
Sodium 0.8.x (mod id `sodium`): `net.caffeinemc.mods.sodium.client.world.LevelSlice` (`private final ClientLevel level`,
`getBlockTint(BlockPos, ColorResolver)`). Embeddium 1.0.x (mod id `embeddium`):
`org.embeddedt.embeddium.impl.world.WorldSlice` (`public final ClientLevel world`, same method).

## Pack integration runbook (once the jar exists)

- Audit DB: `/var/home/azziee/Documents/Azziecraft/build/full_audit.json` — dict keyed by
  slug; entries `{title, ptype, group, client_side, server_side, version_number,
  version_type, date, version_id, filename, url, size, sha1, sha512, deps}`; external
  entries add `source/source_url/project_id/file_id/author/license/notes` (see key
  `going-nuclear`).
- Add `destroy` (external; source github.com/maddlemon/Destroy; release asset URL) and
  `petrolpark` (Modrinth maven file
  `https://api.modrinth.com/maven/maven/modrinth/petrolpark/1.21.1-1.5.11/petrolpark-1.21.1-1.5.11.jar`,
  or resolve the file URL via the Modrinth API).
- Dep closure: `python3 build/depcheck_v2.py` — destroy requires
  minecraft/create/petrolpark/neoforge (all satisfied; pack has NO create-big-cannons,
  NO embeddium; HAS jei, curios, farmers-delight, sodium, iris). Petrolpark's Library
  requires neoforge [21.1.238,) and create [6.0.10,6.0.11).
- Rebuild: `python3 build/build_packs.py` → `Maddiecraft-1.21.1-neoforge.mrpack` +
  `Maddiecraft-1.21.1-neoforge-prism.zip` (listing-only — no mod files inside, per the
  owner's request; launchers download on import). Verify with
  `python3 build/final_verify2.py`. Update `modlist-1.21.1-neoforge-short.md`
  (pack doc, in `/var/home/azziee/Documents/Azziecraft/`).
- Pack facts: 118 files (116 Modrinth mods + 1 CurseForge external + 1 resource pack);
  NeoForge 21.1.262; Create 6.0.10; Create's Modrinth version lists no deps
  (flywheel/ponder are jar-in-jar inside Create's jar).

## Server box runbook (queued — not started)

- sshd is on **port 22** (17171 refused). User `azziee`. Key file `netbox` in
  `/var/home/azziee/Documents/Azziecraft/` (passphrase-protected; the passphrase and
  the box address are deliberately kept out of this public repo). Copy to
  `~/.ssh/netbox`, `chmod 600`, connect with `-o IdentitiesOnly=yes` (an agent key
  otherwise causes "too many authentication failures"); supply the passphrase via an
  `SSH_ASKPASS` script + `SSH_ASKPASS_REQUIRE=force`.
- Box: Java 21.0.12.1 at `/usr/bin/java`, 8 cores, 15 GB RAM, ~340 GB free. Home has
  old experiments: `mc/`, `mc2/` (old server dirs), `falcon.mrpack`,
  `mrpack-install-linux` (Modrinth pack installer), `prism-build.tar.zst`, `mantis/`, `sayer/`.
- Plan: `~/maddiecraft/` — install the NeoForge 21.1.262 server (installer jar from
  maven.neoforged.net), place server-side mods (download all via
  `build/download_jars.py` → `build/jars`, sha1-verified; the server needs the
  audit entries with `server_side=true`), accept `eula.txt`, set
  `server.properties` (view-distance 10, simulation-distance 8, motd Maddiecraft),
  `start.sh` with 8–10 GB heap + Aikar flags (per the pack doc), launch detached
  (screen/tmux), verify "Done" in logs, default port 25565.
- Loader exports already exist (listing-only) in `/var/home/azziee/Documents/Azziecraft/`:
  `Maddiecraft-1.21.1-neoforge.mrpack` (Modrinth App / Prism import) and
  `Maddiecraft-1.21.1-neoforge-prism.zip` (Prism skeleton).
