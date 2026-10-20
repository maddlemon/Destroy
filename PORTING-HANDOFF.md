# Destroy → NeoForge 1.21.1 port — hand-off

Port of **Destroy** (Create chemistry add-on, upstream: `Petrolpark-Mods/Destroy`)
from **Forge 1.20.1 → NeoForge 1.21.1** (MC 1.21.1, NeoForge 21.1.262, Create
6.0.10-280) for the **Maddiecraft** modpack. When the jar builds, publish a
GitHub release on this fork and add the mod + Petrolpark's Library to the pack.

- Branch: `1.21.1-neoforge` (base: upstream `1.20.1` @ `851d5d55`)
- Package moved `com.petrolpark.destroy` → `petrolpark.mc.destroy` (author's
  NeoForge-branch convention)
- 777 java files under `src/main/java/petrolpark/mc/destroy/`

## Build

On Windows (Maddie's machine): JDK 21 is `JAVA_HOME`; from Git Bash run
`./gradlew compileJava --console=plain > compile.log 2>&1` and read the `N errors` line.
NFRT artifacts were already cached, so the first compile took ~1.5 min.

Working dir is `/var/home/azziee/Documents/Azziecraft/build/destroy` — do **not**
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
- Parse a compile log: `python3 /var/home/azziee/Documents/Azziecraft/build/parse_errors.py <log>`
- Jar: `./gradlew build` once compile is clean.
- Migration scripts (idempotent, rerunnable) live in
  `/var/home/azziee/Documents/Azziecraft/build/`: `phase_a.py`, `phase_a2.py`,
  `phase_a3.py`, `manual_edits.py`, `resources_migrate.py`, `phase_b1.py`.
- javap inspection: `$JDK/bin/javap -classpath <dir> <class>`; dependency jars are in
  `~/.gradle/caches/modules-2/files-2.1/` (create, petrolpark, pquality, registrate,
  ponder, jei, flywheel) — extract classes from there instead of re-downloading.

## Environment gotchas (sessions get killed)

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

## Progress

| Commit | Contents |
|---|---|
| `381a135f` | Phase A: `git mv com/petrolpark/destroy → petrolpark/mc/destroy`, grafted the author's 59 NeoForge files, ordered mechanical rewrite (810 files; `new ResourceLocation(...)` → `parse`/`fromNamespaceAndPath`; slash-form mixin descriptors) |
| `7de3041f` | Scaffolding: library imports, tag namespaces (`c:`), config facade, compat cleanup (dropped CBC/JEI/curios compat), resources migration (163 JSON), build files, mixins json |
| `21063ac6` | `gradlew` executable bit |
| `29815ce4` | `DestroyTagDatagen` syntax fix; `-Xmaxerrs 10000` on JavaCompile |
| `f7a0c130` | Phase B batch 1 (mechanical): `DestroyLang` import fixes (`client.`→`util.`, incl. `TemperatureUnit`), `ForgeCatnipServices`→`CatnipServices`, static `AllTags` import fixes, `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, …)`→`Destroy.unsafeRunClient(…)`, `RegistryObject`→`DeferredHolder` fields (`DestroyParticleTypes`, `DestroyRecipeTypes`), nested `Pollution.PollutionType` / `SeismographItem.Seismograph(.Mark)` import fixes, `saturationMod(`→`saturationModifier(` |

| `(this commit)` | AT fix (above); Create 6 `ProcessingRecipeParams` import; `HolderLookup.Provider registries` added to all `SmartBlockEntity`/`BlockEntityBehaviour` `read`/`write`/`writeSafe` overrides + super/behaviour/`FluidTank` NBT calls inside them (28 files); `FluidStack.loadFluidStackFromNBT`/`writeToNBT` -> `parseOptional`/`saveOptional` in block entities; Blowpipe `readBlowing`/`writeBlowing` take a provider. Done from Maddie's Windows machine. |

| `(next commit)` | Tick events -> `ClientTickEvent.Pre/Post`, `PlayerTickEvent.Post`, `LevelTickEvent.Post` (all 9 files; `ExtendedInventoryClientHandler.tick()` no-arg; dropped duplicate `SeismometerItemRenderer.tick()` call, it self-subscribes now); old `PollutionType.SMOG/GREENHOUSE/ACID_RAIN/OZONE_DEPLETION` -> `DestroyPollutionTypes.X.get()`, `pollutionEnabled()` -> `isPollutionEnabled()`; pollution ponder storyboards disabled with `TODO(port)` (author's `PollutionPonderScenes` is fully commented out upstream); `ItemProviderEntry<?>` -> `<?, ?>`. |

**Behaviour note (tick events):** Forge fired Player/Level tick events twice per tick (START+END).
`ChemistryHazardHelper.onPlayerTick`, `PlayerCrouchingCapability`, `PlayerPreviousPositionsCapability`,
`RedstoneProgrammerItemHandler.onLevelTick` and `DestroyCommonEvents.onLevelTick` never checked the
phase, so they ran 2x/tick. They're now on `Post` only = 1x/tick: crouch/urinate counters, ozone-cancer
roll, previous-position sampling and global pollution decay run at half their old rate. Subscribe to
`Pre` too if the old rates matter.

Latest compile: **1,940 errors**.

Still TODO for pollution consumers (old enum API, needs redesign against the author's registry +
data-map `PollutionType.Properties`): `pollutionType.local` / `.max`, `PollutionType.values()`,
`PollutionHelper.pollute(...)` (10 sites), `changePollutionGlobal`, `getCropGrowthFailureParticles`.

Previous compile (after the AT commit): **2,050 errors** (javac count; gradle prints each twice).
Before: 2,178 (after batch 1, javac count). Remaining big buckets: `LazyOptional`/`Capability`
(batch 3), `getOrCreateTag` (batch 3), packets (batch 3), Create `FluidIngredient` (batch 2).

Upstream bug spotted (left as-is): `ExtendedBasinBehaviour` writes `"EvaporatedFluid"` but
reads `"EvaporatedFluidStack"`, so the evaporated fluid never survives a reload.

Previous count (after batch 1): **2301 error lines**. Mix: cannot-find-symbol 1205,
no-override 333, incompatible-types 185, bad-args 133, package-missing 99,
private-access 85, ambiguous 59, no-suitable 53, not-abstract 43, static-ref 9,
inaccessible 5, final-class 2, name-clash 2.

## Resolved: project access transformers were not applied

Root cause: every entry in `META-INF/accesstransformer.cfg` used **SRG names**
(`f_46012_`, `m_280585_(...)`). Since 1.20.5 NeoForge runs on Mojang names in dev *and*
prod, so SRG entries match nothing and NFRT skips them silently. Fixed by rewriting each
entry to the Mojang name from its `#` comment, then checking every entry with `javap`
against `build/moddev/artifacts/neoforge-21.1.262-merged.jar`. Stale entries fixed:
`Gui.renderSlot` (float -> `DeltaTracker`), `SpriteSources.register` (`Codec` -> `MapCodec`),
`PotionBrewing.POTION_MIXES/CONTAINER_MIXES` -> instance fields `potionMixes/containerMixes`
(callers must now go through `level.potionBrewing()`, not static access), and the 4
`ShapedRecipe` entries removed (pattern logic moved to `ShapedRecipePattern` in 1.21).
`validateAccessTransformers = true` is now on in build.gradle, so a bad entry fails the
build instead of being ignored. The `files()` -> `from()` fix was also needed.

## Remaining work (Phase B)

Quick mechanical first:

- `import net.minecraft.core.BlockSource;` → `import net.minecraft.core.dispenser.BlockSource;`
  (8 files; the interface moved packages in 1.21). Vanilla dispenser `execute(BlockSource, ItemStack)` signature unchanged.

Batch 2 — API fixes (verified against the jars):

- Create 6 recipe API (imports DONE): `ProcessingRecipeParams` is now a **top-level** class
  `com.simibubi.create.content.processing.recipe.ProcessingRecipeParams` (was
  nested in `ProcessingRecipeBuilder`); the recipe factory is
  `ProcessingRecipe$Factory<P,R>` (was `ProcessingRecipeBuilder.ProcessingRecipeFactory`). Still TODO: `Factory` takes **two**
  type params now, so `DestroyRecipeTypes` and `CircuitDeployerApplicationRecipe.Serializer`
  need real rework; `new ProcessingRecipeBuilder(...)` (abstract now) at ~10 sites.
- Fluid ingredients are NeoForge `net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient`
  (recipes take `SizedFluidIngredient`, not the old Create `FluidIngredient`); Create 6
  keeps a deprecated `com.simibubi.create.foundation.fluid.FluidIngredientOld`. Destroy's
  custom fluid ingredients (`core/recipe/ingredient/fluid/*`) must be reworked to
  NeoForge's `FluidIngredient`/`SizedFluidIngredient`.
- `AllTags.forgeItemTag/forgeBlockTag/forgeFluidTag` → `commonItemTag/commonBlockTag/commonFluidTag`.
  `AllTags` has **no** `tagBlockAndItem` — use `commonItemTag` + `commonBlockTag`.
- `Explosion.center()` returns `Vec3` (no `getPosition()`); `Explosion.level` is
  private — an AT entry `protected net.minecraft.world.level.Explosion f_46012_ # level`
  exists but compile still reports private: investigate AT application.
- `FluidTank.writeToNBT(HolderLookup.Provider, CompoundTag)` — DONE inside block-entity
  read/write; remaining sites are outside those methods (item tanks, capabilities).
- `MobEffectInstance.getEffect()` → `Holder<MobEffect>`.

Batch 3 — structural:

- Packets: ~24 old packet classes + `DestroyMessages` + ~38 send sites → Catnip
  payloads following the author's `DestroyPackets` enum pattern (record +
  `STREAM_CODEC` + register in enum + `CatnipServices.NETWORK.sendToServer/sendToPlayer…`).
  Delete old packets + `DestroyMessages`; fix `SyncChunkPollutionS2CPacket` refs
  (ServerLevelMixin, DestroyCommonEvents, DestroyMessages).
- `LazyOptional` → direct nullable (29 files; `getCapability` returns `@Nullable T`).
- 5 custom capabilities → attachments: `EntityChemicalPoisonCapability`,
  `PlayerBabyBlueAddictionCapability`, `PlayerCrouchingCapability`,
  `PlayerNovelCompoundsSynthesizedCapability`, `PlayerPreviousPositionsCapability` —
  follow `DestroyAttachmentTypes` + the grafted `LevelPollution`/`ChunkPollution`/`ChunkCrudeOil` pattern.
- `getOrCreateTag` (76 sites) + `FluidStack.getOrCreateChildTag("Mixture")` → DataComponents:
  add a MIXTURE `DataComponentType<CompoundTag>` (`ExtraCodecs.COMPOUND_TAG` +
  `ByteBufCodecs.COMPOUND_TAG`) to `DestroyDataComponentTypes`; use
  `DataComponents.MAP_ID` for the cartography "map" int and `DataComponents.POTION_CONTENTS`
  for the potion string. `INBTSerializable` (5 capability files — rewritten anyway).

Batch 4:

- Tick events (9 files: FogHandler, RedstoneProgrammerMenu, RedstoneProgrammerItemHandler,
  PlayerCrouchingCapability, PlayerPreviousPositionsCapability, ChemistryHazardHelper,
  DestroyClientEvents, DestroyCommonEvents, ExtendedInventoryClientHandler) →
  `event.tick.*TickEvent` Pre/Post + `client.event.ClientTickEvent`;
  `phase==START`→Pre, `==END`→Post, `side==LogicalSide.SERVER`→`!event.getEntity().level().isClientSide()`.
- Ponder scenes: `OilPonderScenes` (54 errors — old nested `SeismographItem.Seismograph/Mark`),
  `DestroyPonderScenes` → `PollutionPonderScenes` refs.
- Contamination (`CircuitPatternItemAttribute`, `FireproofingHelper`) — library classes
  gone; reimplement minimal. Decay (`CarboxylatingItem`, `OxidizingItem`) →
  `StackableFlashingDecayingItem`/`ItemDecay`.
- Recipe system: `DestroyRecipeTypes` still imports
  `com.petrolpark.recipe.advancedprocessing.AdvancedProcessingRecipeSerializer` →
  `petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe$Serializer`.
- `DestroyCreativeModeTabs` — library `CustomTab`/`ITabEntry` API changed.
- Client model code (BakedModelWrapper/geometry/ModelEvent — same paths in 21.1,
  signatures may differ). `SwissArmyKnifeItem` ToolActions — gone; use vanilla
  `net.minecraft.world.item.TierSortingRegistry`. Crafting ingredients
  (`IIngredientSerializer`/`StrictNBTIngredient`/`AbstractIngredient`) →
  `ICustomIngredient` + `IngredientType`; brewing OK at `common.brewing`.

Then: iterate compile until clean → `./gradlew build` → commit → push → GitHub
release with the jar.

## Verified API mappings (NeoForge 21.1.262 / MC 1.21.1 / Create 6.0.10 / library 1.21.1-1.5.11)

NeoForge: `NeoForge`, `NeoForgeMod`, `ModConfigSpec`, `DeferredHolder<R,T>` (RegistryObject
GONE), `DeferredRegister` (+`.DataComponents`), `Capabilities.{FluidHandler,ItemHandler,EnergyStorage}.{BLOCK,ITEM}`,
`RegisterCapabilitiesEvent`, `BlockCapability/ItemCapability/EntityCapability/LevelCapability`,
`AttachmentType`, `event.tick.{Entity,Level,Player,Server}TickEvent` (Pre/Post nested),
`client.event.ClientTickEvent.Pre/Post`, `EventHooks` (was ForgeEventFactory), `IShearable`,
`IEntityWithComplexSpawn`, extensions `IItemExtension/IBlockExtension/IBlockStateExtension/IBlockEntityExtension/IEntityExtension/IMenuTypeExtension`,
`RegisterGuiLayersEvent`, `VanillaGuiLayers` (at `client.gui`, was `client.gui.overlay.VanillaGuiOverlay`),
`DeferredSoundType` (at `common.util`, was ForgeSoundType), conditions at
`net.neoforged.neoforge.common.conditions` (ids `neoforge:mod_loaded|false|true|not|and|or|tag_empty|item_exists`;
`"conditions"` key still works in recipes), `ModConfigEvent implements IModBusEvent`,
`net.neoforged.fml.ModContainer`, `ModLoadingContext` + `LogicalSide`, `InterModComms`.
GONE: ToolActions/ToolAction, PlantType, TierSortingRegistry (use vanilla
`net.minecraft.world.item.TierSortingRegistry`), ForgeHooks, ForgeGui, `registries.tags.ITag`,
`Capability/CapabilityManager/CapabilityToken/LazyOptional`, the whole network stack
(SimpleChannel/NetworkRegistry/NetworkHooks/NetworkEvent/NetworkDirection; PacketDistributor exists),
DistExecutor, FMLJavaModLoadingContext.

MC 1.21.1: `FoodProperties.Builder.saturationModifier(float)`; `Explosion.center()` → Vec3;
`CriteriaTriggers.register(ResourceLocation, trigger)`; `ItemStack` `getOrCreateTag/getTag/setTag`
GONE (use DataComponents); `ItemTags.create/BlockTags.create/FluidTags.create` GONE
(use `TagKey.create(Registries.X, id)`); dispenser `BlockSource` at `net.minecraft.core.dispenser`;
tag dirs singular (`tags/block`, `tags/item`, `tags/fluid`); `pack.mcmeta` pack_format 48 +
`supported_formats: [0, 2147483647]`, no forge:/neoforge: split keys.

Tags: all conventional under `c:` — `c:strings` (was forge:string), `c:leathers`,
`c:glass_blocks` (+`/colorless`; covers all 16 stained colors via `c:glass_blocks/cheap`),
`c:dusts|ingots|ores|storage_blocks|plates|rods|seeds|raw_materials|nuggets|dyes|gems|crops|tools|buckets`,
`c:foods/raw_meat`, fluid `c:milk`, `c:stones|gravels|cobblestones|sands|netherracks`,
`c:sandstone/blocks` (was forge:sandstone). biome_modifier: `data/<ns>/neoforge/biome_modifier`
+ `"type": "neoforge:add_features"`.

Create 6: `CreateRegistrate`, `TagGen$CreateTagsProvider`, `compat.jei`
(`ConversionRecipe`, `MysteriousItemConversionCategory`, `SequencedAssemblySubCategory$AssemblyDeploying`
public no-arg ctor); `IAssemblyRecipe.getJEISubCategory` ABSTRACT →
`Supplier<Supplier<SequencedAssemblySubCategory>>`; `GogglesItem.addIsWearingPredicate(Predicate<Player>)`;
`ProcessingRecipeBuilder` has no nested params/factory (see batch 2).

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
`AbstractPetrolparkRegistrate` (NO setTooltipModifierFactory).
GONE from library: contamination classes, SimpleAdvancementTrigger, C2SPacket/S2CPacket.
Catnip classes (274, incl. `CatnipServices`, `CatnipPacketRegistry`, `ConfigBase`,
`BasePacketPayload`, `ScreenOpener`, …) are shaded inside Ponder's jar — no Catnip
mod/dependency needed.

## Decisions

- Dropped `compat/createbigcannons` (CBC not in pack; 5.11.x API incompatible),
  `compat/jei` (45 files; JEI 15→19 too large/risky; author dropped it too),
  `compat/curios` (1 file, never wired). Kept `compat/pquality` (author kept it).
- Deleted mixins: `compat.jei.*` (7), `compat.embeddium.client.WorldSliceMixin`
  (embeddium not in pack), `compat.farmersdelight.CuttingBoardMixin` (FD 1.21.1 uses
  `useItemOn`; old `use` descriptor would fail at runtime).
- Config: old config tree kept (Catnip `ConfigBase`-compatible); author's `DestroyConfigs`
  = engine (`@EventBusSubscriber(bus = Bus.MOD)`); NEW `config/DestroyAllConfigs.java` =
  thin facade (`CLIENT/COMMON/SERVER` fields + `register(ModLoadingContext, ModContainer)`)
  so all `DestroyAllConfigs.SERVER.x` refs work.
- `DestroyAdvancementTrigger`: `SimpleAdvancementTrigger` → `PlayerTrigger`
  (`CriteriaTriggers.register(Destroy.asResource(e.triggerId), e.trigger)`).
- `Destroy.java`: ctor `Destroy(IEventBus modEventBus, ModContainer modContainer)`;
  static tooltip block removed (no library tooltip factory); compat init lines removed;
  `DestroyMessages.register()`→`DestroyPackets.register()`; `+DestroyCriteriaTriggers.register()`;
  `runForDist/unsafeCallClient/unsafeRunClient` helpers added.
- Resources: pack.mcmeta ×3 → 48 + supported_formats; biome_modifier →
  `data/destroy/neoforge/biome_modifier` + `neoforge:add_features`; conditions `forge:`→`neoforge:`;
  generated `data/forge/tags/{blocks,items}` → `data/c/tags/{block,item}`
  (glass→glass_blocks, glass/colorless→glass_blocks/colorless, string→strings);
  `data/forge/tags/fluids` → `data/destroy/tags/fluid`; singularized tag dirs;
  content seds (fluidTag urine/crude_oil/apple_juice→`destroy:`, milk→`c:milk`,
  chocolate→`c:chocolate`; `"forge:X"`→`"c:X"`; `#forge:`→`#c:`); 0 leftover `forge:` tokens.
  mixins json: JAVA_21 + 10 compat lines removed.

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
