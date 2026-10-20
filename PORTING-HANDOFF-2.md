# Destroy → NeoForge 1.21.1 port — hand-off #2 (step 5, mid-flight)

**Read `PORTING-HANDOFF.md` first** — it is authoritative for steps 1–4, the build commands,
environment gotchas, established patterns, verified API mappings and the pack/server runbooks.
This document covers the step-5 (remaining API changes) work sessions after it was written,
and the state at hand-off. Two humans work on this: Azzie (Linux box, this machine) and
Maddie (Windows, git identity `Meowfoe`). **Pull before starting.**

## State at hand-off

- Branch `1.21.1-neoforge`. Steps 1–4 (recipes, packets, pollution / crude oil / seismology,
  item capabilities + NBT) are done and pushed through `a7496783`. **Step 5 is in progress.**
- Compile: **166 deduped javac errors** (334 error lines, compile11 — the state of the pushed
  tree). Error history: 567 (start of step 5) → 327 (compile6) → 213 (compile8) → 178 (compile9)
  → 166 (compile11).
- **Windows (Maddie): 128 unique errors / 258 error lines** after the decay-item batch
  (`python port-tools/errsummary.py <log>`; the same tree as compile11 counted 164 / 332 there;
  149 / 302 after the block `codec()` batch).
- Latest logs on Azzie's box: `/var/home/azziee/Documents/Azziecraft/build/compileN.log`;
  parse with `python3 /var/home/azziee/Documents/Azziecraft/build/parse_errors.py <log>`.
- Everything below is committed and pushed in the hand-off commit (see History). The working
  tree was clean apart from build outputs.
- WIP safety backup (pre-hand-off): `/var/home/azziee/Documents/Azziecraft/build/wip-backup/`
  (`step5-modified.patch` + `untracked.tar.gz`). Superseded by the commit, kept for reference.

## What the previous session(s) did (other agents' WIP, now committed)

- **`DestroyCommonModEvents`**: `ModFilePackResources` helper → two
  `AddPackFindersEvent.addPackFinders(...)` calls, keeping the 1.20.1 names, positions,
  sources and "always on" flags:
  `event.addPackFinders(Destroy.asResource("resourcepacks/create_patches"), PackType.CLIENT_RESOURCES, Component.literal("Destroy Patches For Create"), PackSource.BUILT_IN, true, Pack.Position.TOP)`
  and `("datapacks/tfmg_compat", PackType.SERVER_DATA, Component.literal("Destroy Compat With Create: TFMG"), PackSource.DEFAULT, false, Pack.Position.TOP)`.
  Signature verified against NeoForge 21.1.262 (impl does `ModList.getModContainerById(...)`
  + `modFile.findResource(...)`); both pack dirs exist.
- **`EntityAttributeModificationEvent`**: passes `Holder`s directly now.
- **New file** `core/registrate/builder/DestroyBlockEntityBuilder.java`: extends the library's
  `PetrolparkBlockEntityBuilder<T,P>`, adds `visual(NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>>)`
  + `registerVisualizer()` (copies Create 6's `CreateBlockEntityBuilder`: `CatnipServices.PLATFORM
  .executeOnClientOnly(() -> this::registerVisualizer)`; `OneTimeEventReceiver.addModListener(
  getOwner(), FMLClientSetupEvent.class, $ -> SimpleBlockEntityVisualizer.builder(getEntry())
  .factory(visualFactory.get()).skipVanillaRender(be -> !renderNormally.test(be)).apply())`).
  **Ctor is `public`** (it is called from `AbstractDestroyRegistrate`, a different package —
  a `protected` ctor was compile error `AbstractDestroyRegistrate.java:64`, fixed this session).
- **`AbstractDestroyRegistrate`**: added `virtualFluid(String, ResourceLocation, ResourceLocation,
  FluidTypeFactory, NonNullFunction, NonNullFunction)` + 2-arg overload (both build a
  `VirtualFluidBuilder`, unchecked-cast to `FluidBuilder<T,R>`), `displaySource(String, Supplier<T>) `
  (returns Create 6 `com.simibubi.create.api.registry.registrate.SimpleBuilder` with `.byBlock(
  DisplaySource.BY_BLOCK).byBlockEntity(DisplaySource.BY_BLOCK_ENTITY)`), and `blockEntity` overrides
  returning `DestroyBlockEntityBuilder`.
- **`SmartExplosion`**: 1.21.1 `Explosion` ctor (extra `ParticleTypes.EXPLOSION,
  ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE` — note `GENERIC_EXPLODE` is a
  `Holder.Reference<SoundEvent>` here, so `.value()` is correct where a raw `SoundEvent` is needed);
  `ignoreExplosion(this)`; blast-protection knockback via
  `Attributes.EXPLOSION_KNOCKBACK_RESISTANCE` (0.15/level, matches Forge's dampener);
  `getDamageSource()` → AT'd `damageSource` field; `getExperienceReward((ServerLevel) level, null)`;
  network write/read via `ClientboundExplodePacket.STREAM_CODEC`.
- **`SimpleDyeableNameableMixedExplosiveBlockEntity`**: `GameEventListener.Holder` →
  `GameEventListener.Provider`; `canReceiveVibration`/`onReceiveVibration` take `Holder<GameEvent>`.
- **`ExtendedBasinBehaviour`**: Create 6 `BlockEntityBehaviourEvent` is not generic and has no
  `getBlockEntity()` — uses `forType(...)`; **fixed this session**: `BasinBlockEntity.BASIN` does not
  exist in Create 6, it is `AllBlockEntityTypes.BASIN` (`BlockEntityEntry<BasinBlockEntity>`).
- **`DestroyPotatoProjectile*HitActions`**: `Codec` → `MapCodec`, `RecordCodecBuilder.create` →
  `mapCodec`, `codec()` returns `MapCodec<? extends ...>` (Create 6 registries hold MapCodecs).
- **Untracked new files** (now committed, **wiring still TODO** — see below):
  `DestroyJukeboxSongs.java`, `core/item/CombustibleItem.java`, `mixin/LivingEntityMobEffectMixin.java`
  (listed in `destroy.mixins.json`), `util/BlockUseHelper.java`,
  `src/main/resources/data/destroy/jukebox_song/{blank,spectrum}.json`.

## What this session did (also committed)

- **phase_b2 (mechanical API renames, script `build/phase_b2.py`)**: 7 renderers
  `CatnipServices.FLUID_RENDERER` → `NeoForgeCatnipServices.FLUID_RENDERER` (SiphonRenderer,
  AgeingBarrelRenderer, BubbleCapRenderer, SimpleMixtureTankRenderer, ElementTankRenderer,
  VatSideRenderer, VatRenderer); `DestroyFoods` `.alwaysEat()` → `.alwaysEdible()`;
  `getRawFluid()` → `getFluid()` (GeniusFluidTankBehaviour, AgeingBarrelBlockEntity,
  BubbleCapBlockEntity); `setSecondsOnFire(3)` → `igniteForSeconds(3)` (BlowpipeItem,
  AbstractMoltenBlock, SpontaneouslyCombustingItem); `hasCustomHoverName()` →
  `has(DataComponents.CUSTOM_NAME)` (ExtendedInventory, MixedExplosiveBlockItemRenderer);
  TestTubeRackBlock `getBlockReach()` → `blockInteractionRange()`; SyringeItemRenderer
  `getUseDuration()` → `getUseDuration(player)`; BucketAndSpadeItem / BuildSandCastleGoal →
  `hurtAndBreak(...)` (residuals fixed below); AbstractMoltenBlock `getFeetBlockState()` →
  `getInBlockState()` + `pickupBlock(@Nullable Player, LevelAccessor, BlockPos, BlockState)`;
  ChemistryHazardHelper `stack.isEdible()` → `stack.getFoodProperties(player) != null`;
  IItemReactant ITag → `BuiltInRegistries.ITEM.getTag(tag).ifPresent(...)`; ExtendedInventory:382
  fully-qualified `NeoForgeRegistries.ITEMS.getKey` → `BuiltInRegistries.ITEM.getKey`;
  DestroyVillagers → `Registries.POINT_OF_INTEREST_TYPE` / `Registries.VILLAGER_PROFESSION`,
  `poi.is(AGING_BARREL_POI)` (residual `.value()` fixed below); DestroyParticleTypes →
  `Registries.PARTICLE_TYPE`; DestroyTags `containsTag(tag)` → `is(tag)`,
  `matches(MobEffect)` → `matches(Holder<MobEffect>)` returning `effect.is(tag)`;
  DestroyPonderTags `RegistryEntry<?, ?>` + `BuiltInRegistries.ITEM.getKey(itemLike.asItem())`;
  DestroyStressConfigs `BuiltInRegistries.BLOCK.getKey(block)`; MilkCartonItem →
  `effect.getCures().contains(EffectCures.MILK) && effect.getEffect().value().getCategory() ==
  MobEffectCategory.HARMFUL` then `removeEffect(effect.getEffect())`; VatSideBlock `getDrops` →
  `ResourceKey<LootTable>` + `reloadableRegistries().getLootTable(lootKey)` + deleted 3-arg
  `getCloneItemStack` override; PumpjackCamBlock + PumpjackStructuralBlock `playerWillDestroy`
  returns `BlockState`; StackedTextBox `render` → `protected renderWidget` (+ super call);
  MechanicalSieveVisual + TreeTapVisual `beginFrame(DynamicVisual.Context)`; DestroyFluids
  helper return types `FluidBuilder<VirtualFluid, DestroyRegistrate>`.
- **phase_b3 (script `build/phase_b3.py`)** + manual: see "previous session(s)" above.
- **AT saga — RESOLVED, do not re-add.** A `public …BlockBehaviour skipRendering(BlockState,
  BlockState, Direction)Z` entry was appended to `src/main/resources/META-INF/accesstransformer.cfg`;
  it broke the NeoForm `createMinecraftArtifacts` recompile ("attempting to assign weaker access
  privileges" in vanilla `PowderSnowBlock` / `LiquidBlock` / `HalfTransparentBlock` / `IronBarsBlock`
  / `MangroveRootsBlock`, which override `skipRendering` as `protected`). The entry is **removed**.
  Instead `VatSideBlock.hidesNeighborFace` now calls the public state-level method:
  `return getMaterial(level, pos).skipRendering(neighborState, dir);`
  (`BlockStateBase.skipRendering(BlockState, Direction)` → `materialBlock.skipRendering(
  materialState, neighborState, dir)`).
  **Behaviour note:** the own-state argument is now the *material's* state instead of the
  (property-less) VatSide state. No vanilla `skipRendering` impl uses the own-state argument, and
  Destroy's `BorosilicateGlassFiberBlock.skipRendering` does `state.getValue(MOLTEN)`, which would
  have **crashed** on a VatSide state — a latent 1.20.1 bug, now fixed. Net behaviour is the same
  or better. (Verified: `IBlockExtension.hidesNeighborFace` default returns `false` — do NOT call
  the block-level `hidesNeighborFace` on the material, that loses the `skipRendering` logic.)
- **Particle data rewrite (verified clean in compile9)**: `GasParticleData` and
  `BoilingFluidBubbleParticleData` rewritten to the Create 6 pattern (`FluidParticleData`):
  `ParticleOptions.Deserializer`/`writeToNetwork`/`writeToString` deleted (1.21.1 `ParticleOptions`
  is only `getType()`; `ParticleType` now has abstract `MapCodec<T> codec()` + `StreamCodec<? super
  RegistryFriendlyByteBuf,T> streamCodec()`); codecs are `MapCodec`s via `RecordCodecBuilder.mapCodec`;
  per-type `StreamCodec`s (`StreamCodec.composite(FluidStack.STREAM_CODEC, …, ByteBufCodecs.FLOAT, …)`
  for DISTILLATION = fluid + blockHeight, `FluidStack.STREAM_CODEC.map(...)` for EVAPORATION).
  `GasParticleData` **overrides `createType()`** so the anonymous `ParticleType`'s `streamCodec()`
  dispatches on the ParticleType's own identity — the two gas types have *different network shapes*,
  and `ParticleEngine.makeParticle` looks up the provider by `BuiltInRegistries.PARTICLE_TYPE.getKey(
  data.getType())`, so decoded data must carry the correct type (the default `createType()` would
  dispatch on the registration instance's unset `type` field). Also fixes an **upstream bug**: the
  old code wrote `blockHeight` with `writeFloat` but read it with `readInt`.
- **Residual phase_b2/b3 fixes (this session)**: `EquipmentSlot.MAIN_HAND/OFF_HAND` →
  `MAINHAND/OFFHAND` (1.21.1 enum constant names!) in BucketAndSpadeItem + BuildSandCastleGoal;
  added missing `net.minecraft.world.InteractionHand` import (BucketAndSpadeItem); removed a
  duplicate wrong `net.minecraft.world.level.chunk.ChunkStatus` import (BuildSandCastleGoal — the
  correct one is `net.minecraft.world.level.chunk.status.ChunkStatus`); `SoundEvents
  .VILLAGER_WORK_SHEPHERD.value()` → `.VILLAGER_WORK_SHEPHERD` (in 1.21.1 this field is a **raw**
  `SoundEvent**, unlike e.g. `GENERIC_EXPLODE` which is a `Holder.Reference` — check per-field with
  javap); `ExtendedBasinBehaviour` `BasinBlockEntity.BASIN` → `AllBlockEntityTypes.BASIN`;
  `PumpjackCamBlock.getCloneItemStack` → 1.21.1 signature `(BlockState, HitResult, LevelReader,
  BlockPos, Player)`; `AgeingBarrelRenderer:51` + `AgingBarrelBlock:93` `itemCapability.orElse(…)` →
  null-guarded direct field access (`itemCapability` is a plain `IItemHandlerModifiable` field set
  in the BE ctor, not an Optional); `FastCoolingMoltenPillarBlock:38` →
  `EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(
  Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER), livingEntity)` (`Enchantments.X`
  are `ResourceKey`s in 1.21.1 and `BuiltInRegistries` has no `ENCHANTMENT` registry — resolve via
  a `HolderLookup.Provider`, as `DestroyBlocks` already does).

## Wiring TODO (untracked files, committed but not yet referenced)

- `DestroyJukeboxSongs.java` + `data/destroy/jukebox_song/{blank,spectrum}.json`: 1.21.1 jukebox
  songs (`JukeboxSong` registry). Wire registration into the `Destroy` constructor (pattern: every
  `Destroy*` registry class is registered there) and point `DiscStamperItem:16` (errors on the removed
  `RecordItem`) at it. `CombustibleItem`, `LivingEntityMobEffectMixin`, `BlockUseHelper`: check what
  should reference them and wire in.

## Do next (remaining step 5, by theme — compile + commit per batch)

1. **Block `codec()` — DONE** (13 blocks; the other 5 `not-abstract` errors are models / saved data /
   item attribute, in their own items below). Properties-only blocks:
   `public static final MapCodec<X> CODEC = simpleCodec(X::new);` +
   `@Override protected MapCodec<? extends X> codec() { return CODEC; };` (`? extends X` so subclasses can
   override). Blocks with extra ctor args get a `RecordCodecBuilder` codec like vanilla `StemBlock` /
   `LayeredCauldronBlock`: `FullyGrownCropBlock` + subclass `HeftyBeetrootBlock` (`seed` via
   `BuiltInRegistries.ITEM.byNameCodec()`), `TankPeriodicTableBlock` (`color`), `UrineCauldronBlock`
   (`CauldronInteraction.CODEC`). Only the classes that had errors (plus their subclasses) got one;
   plain `Block` subclasses inherit `Block.CODEC` like Create's blocks.
2. **Decay items — DONE**, see "Decay items" below (also cleared the `DestroyReactions:894` cascade).
3. **Contamination** (FireproofingHelper 20, CircuitPatternItemAttribute 16): old
   `com.petrolpark.contamination.*`, `PetrolparkRegistries`, `Contaminables`,
   `HasContaminantItemAttribute`, `LegacyDeserializer`, old library `ItemAttribute` all GONE;
   `Item.isFireResistant()` GONE (1.21.1: check `DataComponents.FIRE_RESISTANT`). Reimplement
   minimally on Create 6 `com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute`
   + library `petrolpark.mc.library.compat.create.core.world.item.attribute.*` — **not yet designed**.
   Note `CircuitPatternItemAttribute.java:73`: anonymous `ItemAttributeType` needs `streamCodec()`.
4. **Plant blocks** (MagicBeetrootShootsBlock 22, FullyGrownCropBlock 12): `PlantType` GONE,
   `EventHooks.getMobGriefingEvent` GONE, `ForgeHooks` usage (2 sites) — inspect.
   MagicBeetrootShootsBlock also needs 1.21.1 `BonemealableBlock.isValidBonemealTarget(LevelReader,
   BlockPos, BlockState)` (was hidden behind the codec error). `PlantType.CROP` can simply go: NeoForge's
   `BushBlock.canSurvive` asks `canSustainPlant` (default `TriState.DEFAULT`) then `mayPlaceOn`, which
   already checks farmland. **Parity catch:** in 1.20.1 any `IPlantable` crop on farmland stopped it
   drying to dirt; 1.21.1 `FarmBlock.shouldMaintainFarmland` only checks the `minecraft:maintains_farmland`
   block tag (vanilla crops only, not `#crops`), so tag Golden Carrots, the Hefty/infused Beetroots and
   Magic Beetroot Shoots with `BlockTags.MAINTAINS_FARMLAND` in `DestroyBlocks`.
5. **Client models**: DestroySpriteSource (12: `SpriteSource.codec()` now returns `MapCodec`,
   `SpriteSources.register(String, MapCodec)`, `PalettedPermutations` `PalettedSpriteSupplier
   .apply(SpriteResourceLoader)`, `AnimationMetadataSection` → `ResourceMetadata`,
   `ForgeTextureMetadata` GONE), DummyBaker (`IModel.bakeUncached(UnbakedModel, ModelState,
   Function<Material,TextureAtlasSprite>)`, `UnbakedGeometryHelper.bake` sig), CircuitPatternItemModel
   (`IUnbakedGeometry.bake(IGeometryBakingContext, ModelBaker, Function, ModelState, ItemOverrides)`),
   MoleculeRenderer:141 (`QuadBakingVertexConsumer` ctor), BlowpipeItemRenderLayer:54 (`renderers`
   private in `EntityRenderDispatcher` — needs AT or accessor mixin).
6. **Ponder/client misc**: DestroyClient (import `client.DestroyPonderPlugin` — the file exists at
   `client/ponder/DestroyPonderPlugin.java`, check what it references), DestroyGuiTextures (14),
   DestroyIcons, DestroyItemDisplayContexts (11: `ItemDisplayContext.create` GONE — no such static
   in 1.21.1), BetterValueSettingsScreen:41, SidedScrollValueBehaviour:174,
   mixin/client/GuiMixin:39 (`Gui.HeartType.getX(boolean,boolean)` GONE → `getSprite(boolean,boolean,
   boolean)`), mixin/client/PonderLevelMixin:34 (`gatherCapabilities()`),
   DestroyClientModEvents:81 (String → ResourceLocation).
7. **`NbtUtils.readBlockPos`** (1-arg GONE; new is `readBlockPos(CompoundTag, String)` →
   `Optional<BlockPos>`): VatSideBlockEntity:218, VatControllerBlockEntity:309, Vat:75 (×2),
   DynamiteBlockEntity:72,73, PumpjackCamBlockEntity:43, ExtrudableMovementBehaviour:77,110.
8. **BlockPos → Vec3** (4): VatControllerBlockEntity:663, Vat:352, DynamiteBlockEntity:86,
   DynamiteBlock:30.
9. **Cauldron — DONE** (with item 1, the codec needs it): `DestroyCauldronInteractions.URINE` is
   `CauldronInteraction.newInteractionMap("destroy:urine")` (registers it in `INTERACTIONS`, default
   `PASS_TO_DEFAULT_BLOCK_INTERACTION` = 1.20.1's `PASS`); the glass-bottle interaction returns
   `ItemInteractionResult.sidedSuccess`.
10. **Loot / advancement / conditions / misc**: DestroyLootConditions (`net.minecraft.world.level
    .storage.loot.Serializer` GONE → `LootItemConditionType` + MapCodec), ObliterationCondition (6),
    DestroyAdvancementTrigger:99,110 (`getAdvancement(ResourceKey.create(Registries.ADVANCEMENT,
    id))` → `AdvancementHolder`), ExplosiveProperties:224, PeriodicTableBlock:109,
    VatMaterialResourceListener:54 (`CraftingHelper.processConditions(JsonObject, String,
    IContext)` GONE → NeoForge `ICondition`/`ConditionalOps`), DestroyReactions:894 (method-ref
    return type), ForgeChemistryEvent (`isCanceled()` — check `ICancellableEvent`),
    DestroyBlockTagsProvider (6: `Blocks.ACID_RAIN_*` — likely `DestroyBlocks` refs),
    ExplosivesPonderScenes (`ItemStack.addTagElement` GONE → `DataComponents.FIREWORKS`;
    `setHoverName` → `set(DataComponents.CUSTOM_NAME, …)`), UniversalArmorTrimItemOverrides +
    UniversalArmorTrimModel (`ArmorTrim.getTrim(RegistryAccess, ItemStack)` GONE;
    `Holder<ArmorMaterial>` → `.value()`; `ModelResourceLocation`), PeriodicTableBlockItem:33
    (`ItemInteractionResult` → `InteractionResult`), DynamoBlock:138 (`Lazy` not a functional
    interface — inspect), MixedExplosiveBlock:133 (no-override), CircuitMaskItem:68,83
    (`DirectionalTransportedItemStack.launch(...)` GONE), KeypunchBlockEntity:94 (`IDirectionalOnBelt`
    → library `compat.create.core.world.item.transported.IDirectionalBeltItem`), CircuitPatternItem
    :19,23 (`NBTHelper.readBinaryMatrix4x4/writeBinaryMatrix4x4` GONE — `petrolpark.mc.library.util
    .BinaryMatrix4x4` exists; reimplement or use its codec), CircuitPatternItemRenderer:61
    (`PetrolparkItemDisplayContexts` GONE), ExtendedInventory:8,162 (`PetrolparkTags.MenuTypes` GONE —
    old tags were `petrolpark:always_shows_extended_inventory` / `never_shows_extended_inventory` /
    `allows_manual_only_crafting` under `tags/menu_type`; decide: Destroy's own tags or check library
    `PetrolparkRegistrateTags`), ExtendedInventoryClientHandler (import
    `client.gui.overlay.ForgeGui` → `net.minecraft.client.gui.Gui`; `VanillaGuiLayers` at
    `client.gui`; `HOTBAR.id()` on ResourceLocation — inspect), CircuitPatternHandler (1.21.1
    `SavedData.save(CompoundTag, HolderLookup.Provider)` sig + `DimensionDataStorage.computeIfAbsent`
    takes a `SavedData.Factory`), DelicateExplosion:22, ExcavationExplosion:71,
    CustomExplosiveMixExplosion:47,163,179 (`ResourceKey<Enchantment>` → `Holder<Enchantment>` via
    `<registryAccess>.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key)` — **`BuiltInRegistries`
    has NO `ENCHANTMENT` registry in 1.21.1**, use the codebase pattern from `DestroyBlocks`;
    `Reference<SoundEvent>` → `SoundEvent` — `.value()`), DestroyItems (8: decay-item cascade, see below).

Then steps 6–9 per `PORTING-HANDOFF.md` (JEI compat, data migration, clean compile → datagen →
build → runtime test, release + pack integration).

## Decay items (done)

Old `com.petrolpark.item.decay.{ConfiguredDecayingItem,IDecayingItem}` GONE. New library model is
**component-based**: `petrolpark.mc.library.registry.PetrolparkDataComponentTypes.{DECAY_TIME
(DecayTime record (String translationKey, long lifetime)), DECAY_PRODUCT (IDecayProduct;
`ChangeItemDecayProduct.of(ItemLike)` / record(ItemStack)), DECAY_START_TIME (Long)}`;
`ItemDecay` statics (`petrolpark.mc.library.core.world.item.decay.ItemDecay`): `checkDecay(stack)`,
`getGameTime()`, `getLifetimeOrNone(stack)`, `getRemainingTime(stack, creationTime)`,
`startDecay(stack, long)`, `extendLifetime(stack, int)` (returns early if no DECAY_START_TIME),
client `getTooltip(stack)` → `Optional<Component>` (uses the component's translationKey, formats
mm:ss). Library `ItemStackMixin` makes `ItemStack.copy()` run `checkDecay`; `DecayingItemDecorator`
draws the flash bar for all items; **no stacking-time merge in the new library** (dropped by the
library authors). `StackableFlashingDecayingItem` (abstract, extends Item): `areDecayTimesCombineable`,
empty `appendHoverText`, `onCraftedBy` → `ItemDecay.startDecay(stack, 0)`, `initializeClient` →
`FlashingDecayingItemRenderer`.

Two facts the original plan missed: the library's `ClientEvents` already adds
`ItemDecay.getTooltip` to **every** Item's tooltip (`ItemTooltipEvent`), so Destroy must not add it again;
and the library itself starts decay on crafting (`ShapedRecipeMixin`, `CraftingMenuMixin`, ...) and Create
processing outputs.

**What was done:** `core/item/DecayingItem` (abstract, extends `StackableFlashingDecayingItem`; ctor
`Properties, Supplier<ItemStack> decayProduct, Supplier<ConfigInt> lifetime, int defaultLifetime,
String translationKey`). `OxidizingItem` / `CarboxylatingItem` extend it with their old translation keys.
- The `DECAY_PRODUCT` / `DECAY_TIME` default components are set in NeoForge's
  `ModifyDefaultComponentsEvent` (`DecayingItem.setDefaultComponents`, subscribed in
  `DestroyCommonModEvents`), **not** in `Item.Properties`: reading a Server config during item registration
  throws (`ModConfigSpec` "Cannot get config value before config is loaded"; Server configs load per world),
  and the event runs once every Item exists, so the product `ItemStack` is safe to build. The prototype
  lifetime is the config's default (`DestroySubstancesConfigs.DEFAULT_SODIUM_DECAY_TIME` = 600,
  `DEFAULT_QUICKLIME_BASE_DECAY_TIME` = 12000, also used by the config entries; Catnip's `ConfigInt` has no
  public default getter).
- `refreshLifetime(stack)` sets `DECAY_TIME` from the live config, server side only, in `onCraftedBy` (before
  `super`, which starts decay) and `inventoryTick`. Setting a value equal to the prototype's removes the
  patch, so with the default config stacks carry no extra component and stack normally.
- `overrideOtherStackedOnMe`: 1.20.1's averaging merge, ported as-is (same conditions, incl. not comparing
  the stacks' other data).
- `OxidizingItem.checkForWater`: `DECAY_START_TIME = gameTime - lifetime`. That is exactly what 1.20.1's
  `extendLifetime(stack, -remaining)` produced in both cases (decay started or not), so one line covers both.

## Process

- Compile (run detached, poll the log):
  `cd /var/home/azziee/Documents/Azziecraft/build/destroy && JAVA_HOME=/var/home/azziee/Documents/Azziecraft/build/jdk21 ./gradlew compileJava --console=plain > /var/home/azziee/Documents/Azziecraft/build/compileN.log 2>&1`
  (cached ≈1.5–3 min; an AT-file change forces a ~3 min NeoForm artifact rebuild — and a bad AT
  entry fails `createMinecraftArtifacts` with "weaker access privileges" errors in vanilla
  subclasses; NeoForge 1.21 AT entries must use **Mojang names** and `validateAccessTransformers`
  is on).
- Work in batches: fix → compile → `parse_errors.py` → repeat. Commit + push after each batch
  that leaves the tree consistent. Commit format:
  `git commit -m "<msg>" -m "Generated by Mistral Vibe.\nCo-Authored-By: Mistral Vibe <vibe@mistral.ai>"`.
- **Ask the user to test the compiled mod after every large step** (they offered debug logs/tests).
- Session kills: `/tmp` is wiped; `~/.ssh/netbox` disappears (re-copy from
  `/var/home/azziee/Documents/Azziecraft/netbox`, `chmod 600`, connect with
  `-o IdentitiesOnly=yes`, passphrase via `SSH_ASKPASS` + `SSH_ASKPASS_REQUIRE=force`; sshd on
  **port 22**, user `azziee`); replays reset exec bits (`chmod -R +x build/jdk21/bin/`,
  `chmod +x gradlew`); `build/stray/com_src_main_java` is a stale duplicate — never restore into `src/`.

## History

| Commit | Contents |
|---|---|
| `f8900f17` | (previous hand-off point) PORTING-HANDOFF.md filled in |
| `5822036a` | Step 5 WIP: other agents' uncommitted work (pack finders, `DestroyBlockEntityBuilder`, `AbstractDestroyRegistrate` additions, SmartExplosion, vibration listener, ExtendedBasinBehaviour, potato projectile MapCodecs, untracked jukebox-song/combustible/mob-effect files) + phase_b2/b3 batches + this session's fixes (AT skipRendering saga resolved, particle data rewrite, residual renames) + this doc. Errors: 327 → 166. |
| `ce2f5524` | (Maddie, Windows) Block `codec()` for 13 blocks + 3 subclasses; urine cauldron `InteractionMap`. Errors (Windows, `port-tools/errsummary.py`): 164 unique / 332 lines → 149 / 302. |
| `COMMIT_DECAY` | (Maddie, Windows) Decay items: `DecayingItem` on the library's decay components (defaults via `ModifyDefaultComponentsEvent`, live config per stack), stacking merge, Oxidizing / Carboxylating items. Errors 149 / 302 → 128 / 258. |
