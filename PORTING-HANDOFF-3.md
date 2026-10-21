# Destroy → NeoForge 1.21.1 port — hand-off #4 (step 6 JEI compat: DONE, client smoke-tested)

**Read `PORTING-HANDOFF.md` (steps 1–4, build, environment, API mappings, runbooks) and
`PORTING-HANDOFF-2.md` (hand-off #3 = Maddie's end-of-session state: clean compile, GameTest
server green, jar built) first.** This document covers the work after that: the recipe load
fixes and the JEI compat restoration (step 6). Two humans: Azzie (Linux, this
machine) and Maddie (Windows, git identity `Meowfoe`). **Pull before starting.**

## State at hand-off

- Branch `1.21.1-neoforge`. Steps 1–7 done. **The whole mod compiles with 0 errors** (JEI included).
- `./gradlew runGameTestServer`: all data loads with zero parse / validation errors, `test/DestroyGameTests` passes.
- **Client smoke test (headless, see runbook below): the dev client joins a world, JEI starts
  ("Starting JEI took ~10 s") with no broken-recipe or Destroy errors, every Destroy JEI mixin applies,
  and the player stays connected.** It found and fixed three runtime bugs (see "Step 6: finish").
- `dist/destroy-1.21.1-0.2.0.jar` is rebuilt from the step-6 fixes — **next: Azzie tests it in a real client.**
- Raw 1.20.1 JEI sources kept at `/var/home/azziee/Documents/Azziecraft/build/jei-1201/` (scratchpad).

## Recipe load fixes (done, commit `448717a7`)

- **Root cause of the 12 failures:** `SingleFluidRecipe`'s ctor forced
  `processingDuration = 20` for every subclass (1.20.1 behaviour, where Create never validated
  at load). Create 6's `ProcessingRecipe.codec(...)` runs `.validate(...)` at parse:
  `processingDuration > 0 && !canSpecifyDuration()` → error. Create 6's
  `ProcessingRecipe.canSpecifyDuration()` default is **false** (0.5.1's was true), and
  `DistillationRecipe` / `ElementTankFillingRecipe` return false. Fix:
  `if (processingDuration <= 0 && canSpecifyDuration()) processingDuration = 20;`
  Verified **no consumer** of `getProcessingDuration()` exists for distillation / element tank
  filling in 1.20.1 or the port (only Ageing, Centrifuge, Dynamo, Sieve + JEI AgingCategory read
  it), so in-game behaviour is unchanged. Also checked: every other recipe type whose JSON sets
  `processing_time` (aging, centrifugation, charging, sieving = SingleFluidRecipe/overrides →
  true; crushing/cutting/milling = Create types → true; electrolysis/arcfurnace/disc-electro =
  Create 6 `BasinRecipe` → `canSpecifyDuration() = true`). 77 JSONs have `processing_time`; all
  fine after the fix.
- **borax biomes:** 1.20.1's library parsed `"biomes"` as an array of `#tag`-or-id strings;
  the new library uses `RegistryCodecs.homogeneousList(Registries.BIOME)` (single tag string
  OR list of direct ids — no mixing). `distillation/borax.json` had
  `["#minecraft:is_badlands", "minecraft:desert"]` → moved to a new tag
  `data/destroy/tags/worldgen/biome/borax_distillation.json` (same biome set) and
  `"biomes": "#destroy:borax_distillation"` (same pattern as brine).
- The hand-off #3 note about `chlorine/hydrogen/mercury` still having `processing_time` keys
  was stale — they don't (Maddie's step-7 commit already cleaned them).

## Step 6: JEI compat restoration (done)

### What was done

1. **Restored all 54 files** from upstream `1.20.1` (git branch `1.20.1`, path
   `com/petrolpark/destroy/compat/jei/**` + `com/petrolpark/destroy/mixin/compat/jei/**` +
   `com/petrolpark/destroy/DestroyMysteriousItemConversions.java`) into the port tree at
   `petrolpark/mc/destroy/...` with a mechanical rename script (raw sources kept in
   `build/jei-1201/`):
   - `com.petrolpark.destroy` → `petrolpark.mc.destroy`
   - `com.petrolpark.compat.jei.category.PetrolparkRecipeCategory` →
     `petrolpark.mc.library.compat.jei.category.PetrolparkRecipeCategory`
   - `com.petrolpark.compat.jei.category.ITickableCategory` →
     `petrolpark.mc.library.compat.jei.category.ITickableCategory`
   - `com.petrolpark.compat.jei.category.builder.PetrolparkCategoryBuilder` →
     `petrolpark.mc.library.compat.jei.category.builder.PetrolparkCategoryBuilder`
   - `com.petrolpark.client.rendering.PetrolparkGuiTexture` →
     `petrolpark.mc.library.core.client.rendering.PetrolparkGuiTexture`
   - `com.petrolpark.compat.jei.JEITextureDrawable` → `petrolpark.mc.library.compat.jei.JEITextureDrawable`
   - `com.petrolpark.compat.jei.JEISetup` → `petrolpark.mc.library.compat.jei.PetrolparkJEISetup`
     (class renamed too: `JEISetup.DECAYING_ITEMS` → `PetrolparkJEISetup.DECAYING_ITEMS`)
   - `com.petrolpark.recipe.ingredient.BlockIngredient` →
     `petrolpark.mc.library.core.data.recipe.ingredient.BlockIngredient`
   - `mezz.jei.api.forge.ForgeTypes` → `mezz.jei.api.neoforge.NeoForgeTypes`
     (`ForgeTypes.FLUID_STACK` → `NeoForgeTypes.FLUID_STACK`)
   - Stale import fixes: `...destroy.client.DestroyLang` → `...destroy.util.DestroyLang`
     (9 files; `DestroyLang` moved in the port's refactoring).
2. **Registered the 8 mixins** in `src/main/resources/destroy.mixins.json`:
   `mixins`: `compat.jei.BasinCategoryMixin`, `compat.jei.CreateJEIMixin`,
   `compat.jei.CreateRecipeCategoryMixin`, `compat.jei.DeployingCategoryMixin`,
   `compat.jei.MixingCategoryMixin`, `compat.jei.PackingCategoryMixin`;
   `client`: `compat.jei.client.GhostIngredientHandlerMixin`,
   `compat.jei.client.JeiProcessingRecipeMixin`.
   Gating is automatic: `PetrolparkMixinPlugin.shouldApplyMixin` skips any mixin under
   `petrolpark.mc.destroy.mixin.compat.<modid>.*` when `<modid>` isn't loaded (works for the
   `compat.jei.client.*` subpackage too). JEI dependency is already in `build.gradle`
   (`mezz.jei:jei-1.21.1-neoforge:19.56.0.441`, bumped by Maddie).
3. **Fix batch 1 (applied):** HeatConditionRenderer (ForgeRegistries →
   `BuiltInRegistries.ITEM.getTag(...).ifPresent(holders -> ... new ItemStack(holder.value()))`;
   `addFluidSlot(builder, x, y, refrigerantIngredient)` →
   `addFluidSlot(builder, x+19, y, new SizedFluidIngredient(refrigerantIngredient, 1000))` —
   Create 6's `addFluidSlot` takes `SizedFluidIngredient`; `SizedFluidIngredient` has a public
   `(FluidIngredient, int)` ctor); MixtureFluidSubtypeInterpreter (Forge FluidStack → NeoForge;
   `hasTag()/getOrCreateTag().getAsString()` → `MixtureFluid.getMixtureTag(stack)` (CompoundTag,
   from the `destroy:mixture` component) → `.isEmpty()` ? NONE : `tag.toString()`);
   MoleculeJEIIngredient (`setHoverName` → `set(DataComponents.CUSTOM_NAME, …)`;
   `new ResourceLocation(String)` → `ResourceLocation.parse(...)`);
   DestroyGhostIngredientHandler (`AllPackets.getChannel().sendToServer(...)` →
   `CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, slotIndex))` — same as
   Create 6's own `compat/jei/GhostIngredientHandler`);
   AnimatedBlowpipe (Forge FluidStack import → NeoForge).
4. **Removed all 14 `super.draw(recipe, recipeSlotsView, …)` calls** in the categories.
   Verified against Create `mc1.20.1/dev` source: 0.5.1's `CreateRecipeCategory` doesn't declare
   `draw` at all — it was JEI 15's empty interface default, so the call was a no-op. Create 6's
   `draw(T, …)` is abstract → the call no longer compiles. **Do not re-add.** (Exception:
   `TripleItemIcon`'s `super.draw(graphics, xOffset, yOffset)` is a real `IDrawable.draw` — kept.)
5. **TYPE fields → holder types:** `ReactionCategory.TYPE` is now `RecipeType<?>`;
   `GenericReactionCategory.TYPE` is `RecipeType<RecipeHolder<GenericReactionRecipe>>`
   (JEI 19's `Info.recipeType()` returns `RecipeType<RecipeHolder<T>>`). Same pattern still
   needed in ArcFurnaceCategory:23, ElectrolysisCategory:28, MixableExplosiveCategory:36.
   `DestroyJEI.DestroyCategoryBuilder.finalizeBuilding` now stores
   `RecipeType.createRecipeHolderType(type.getUid())` (not the raw `RecipeType<R>`) in
   `MIXTURE_APPLICABLE_RECIPE_TYPES`, because JEI 19 looks recipes up by holder type.
6. **`VatMaterialCategory.VatMaterialRecipe`**: 1.21.1 `ShapelessRecipe` ctor is
   `(String group, CraftingBookCategory, ItemStack, NonNullList<Ingredient>)` — dropped the
   `ResourceLocation` first arg.

### Verified API mappings for the remaining work (JEI 19.56 / Create 6.0.10 / library 1.5.11)

- **Create 6 `CreateRecipeCategory<T extends Recipe<?>> implements IRecipeCategory<RecipeHolder<T>>`** —
  the key simplification: the public JEI methods take `RecipeHolder<T>` and delegate to
  `protected abstract setRecipe(IRecipeLayoutBuilder, T, IFocusGroup)`,
  `protected abstract draw(T, IRecipeSlotsView, GuiGraphics, double, double)` and
  `protected List<Component> getTooltipStrings(T, …)` (default `List.of()`). **Destroy's
  categories keep their raw-`T` signatures unchanged** (public override of protected = legal).
  Create 6 statics: `addFluidSlot(builder, x, y, SizedFluidIngredient)` and
  `addFluidSlot(builder, x, y, FluidStack)` (NeoForge), `getRenderedSlot()` /
  `getRenderedSlot(float chance)`, `registerRecipes(IRecipeRegistration)`,
  `registerCatalysts(IRecipeCatalystRegistration)`, `Info` record =
  `(RecipeType<RecipeHolder<T>>, Component, IDrawable, IDrawable, Supplier<List<RecipeHolder<T>>>, List<Supplier<? extends ItemStack>>)`.
- **New library `PetrolparkRecipeCategory<T>`** (`petrolpark.mc.library.compat.jei.category`):
  ctor `(Info<T>, IJeiHelpers)`; `helpers`, `mc`; `addOptionalRequiredBiomeSlot(builder, Recipe<?>, x, y)`
  — takes the **raw** recipe (unwrap the holder); `addOptionalRecipeBookSlot(builder, RecipeHolder<?>, x, y)`;
  `getRegistryAccess()`; `Factory<T> = create(Info<T>, IJeiHelpers)`.
- **New library `PetrolparkCategoryBuilder<R, C>`** (`…compat.jei.category.builder`): ctor
  `(String modid, Class<? extends R>, Consumer<CreateRecipeCategory<?>>)`; `addRecipes(Supplier<Collection<? extends RecipeHolder<R>>>)`;
  `addTypedRecipes(IRecipeTypeInfo)` (filters `recipeClass.isInstance(recipe.value())`);
  `addTypedRecipesIf(Supplier<RecipeType<? extends R>>, Predicate<RecipeHolder<R>>)` and
  `addTypedRecipesIf(Supplier<RecipeType<R2>>, Function<RecipeHolder<?>, RecipeHolder<R>>, Predicate<RecipeHolder<?>>)`;
  `catalyst(Supplier<ItemLike>)`, `catalysts(Collection<Supplier<ItemStack>>)`, `itemIcon(ItemLike)` /
  `itemIcon(Supplier<ItemStack>)`, `doubleItemIcon(...)`, `emptyBackground(w, h)`,
  `enableIfCreateConfig(Function<CRecipes, ConfigBool>)`,
  `build(String, PetrolparkRecipeCategory.Factory<R>)`;
  `protected void finalizeBuilding(mezz.jei.api.recipe.RecipeType<R>, CreateRecipeCategory<R>, Class<? extends R>)`
  — receives the **raw** `RecipeType<R>`.
- **JEI 19:** `RecipeTypes.CRAFTING` = `RecipeType<RecipeHolder<CraftingRecipe>>` (so
  `ExtendedDurationFireworkRocketRecipe.exampleRecipes()`'s `List<RecipeHolder<CraftingRecipe>>`
  fits as-is); `RecipeType.createRecipeHolderType(ResourceLocation)` static;
  `mezz.jei.api.neoforge.NeoForgeTypes.FLUID_STACK` (`IIngredientTypeWithSubtypes<Fluid, FluidStack>`);
  `IModPlugin.registerFluidSubtypes(ISubtypeRegistration, IPlatformFluidHelper<T>)` — signature
  unchanged; `IModIngredientRegistration.register(IIngredientType<V>, Collection<V>, IIngredientHelper<V>, IIngredientRenderer<V>)`
  unchanged; `IVanillaRecipeFactory.createAnvilRecipe(List, List, List, ResourceLocation)` unchanged;
  `IRecipeSlotBuilder`: `addItemStack(s)`, `addIngredient(IIngredientType<T>, T)` /
  `addIngredients(IIngredientType<T>, List<T>)`, `addFluidStack(Fluid, long[, DataComponentPatch])`,
  `addRichTooltipCallback`, `setBackground`; `ISubtypeRegistration.registerSubtypeInterpreter(
  IIngredientTypeWithSubtypes<B,I>, B, ISubtypeInterpreter<I>)`; `IGhostIngredientHandler`
  (`getTargetsTyped`, `onComplete`, `shouldHighlightTargets` default) unchanged;
  `IRecipeManagerPlugin.getRecipeTypes(IFocus<V>)` returns `List<RecipeType<?>>`.
- **Create 6 recipes:** `ProcessingRecipe.getFluidIngredients()` → `NonNullList<SizedFluidIngredient>`;
  `getFluidResults()` → `NonNullList<FluidStack>`; `getRollableResults()` → `List<ProcessingOutput>`
  (`.getStack()`, `.getChance()`); `getRollableResultsAsItemStacks()` → `List<ItemStack>`.
- **Catnip is shaded into the ponder jar** (`net.createmod.ponder:ponder-neoforge` contains
  `net.createmod.catnip.*`): `LangBuilder.component()` / `.string()`,
  `FontHelper$Palette.GRAY_AND_WHITE` / `ofColors`, `TooltipHelper.cutTextComponent(Component, Palette)`
  and `cutStringTextComponent(String, Palette)` all exist.
- **Port facts:** `ArmorMaterial.repairIngredient()` → `Supplier<Ingredient>` (use
  `DestroyArmorMaterials.HAZMAT.value().repairIngredient().get()`); `Item.getRepairIngredient()`
  is gone but the port's `ChemistryProtectionHeadwearItem.getRepairIngredient()` still exists
  (custom public method). Create 6 `ConversionRecipe.create(ItemStack, ItemStack)` returns
  `RecipeHolder<ConversionRecipe>`; `MysteriousItemConversionCategory.RECIPES` is
  `List<RecipeHolder<ConversionRecipe>>`. `AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.tag`
  exists. Create 6 `BasinRecipe.canSpecifyDuration()` returns **true**. Create 6's
  `AnimatedKinetics.blockElement(BlockState)` exists. `HeatCondition` (Create 6) has
  `getTranslationKey()`, `getColor()`, `name()`.
- **Runtime recipe lists now need `RecipeHolder`s with ids:** `ExtrusionRecipe.RECIPES` is
  `List<ExtrusionRecipe>` (raw); `PotionSeparationRecipes.getRecipes(Level)` returns
  `Map<Pair<Holder<Potion>, BottleType>, CentrifugationRecipe>` (raw; needs a `Level` —
  `Minecraft.getInstance().level` in JEI context); `MutationCategory.RECIPES` is
  `List<PhytominingRecipe>` (raw). JEI-side suppliers must wrap: `new RecipeHolder<>(Destroy.asResource("..."), recipe)`
  with unique synthetic ids (the id is only used for JEI identity / the recipe-book slot).

### Step 6: finish (the 69 errors → 0, then runtime fixes)

- **`DestroyJEI.holderOf(recipe)` / `holderOf(id, recipe)` / `holdersOf(collection)`:** JEI 19 shows
  `RecipeHolder`s, Destroy collects plain Recipes. `holderOf` indexes the client Recipe Manager
  (re-indexed when its collection changes) so datapack recipes get their **real** holder (and ID); runtime-built
  recipes get a cached holder — with their 1.20.1 ID where they had a fixed one (`holderOf(id, recipe)`:
  cartography, disc electroplating), otherwise `destroy:jei_generated_<n>`. Cached by identity, so JEI always
  sees the same holder for the same Recipe. Used by every `addRecipes(...)`, the recipe manager plugin's casts,
  and the electrolysis/mixing mixins' extra recipes.
- `ChemicalSpeciesRecipeManagerPlugin`: NeoForge `FluidStack`; the ingredient subtype/molecules are read from
  `MIXTURE_INGREDIENT_DISPLAY`, mixture outputs from `MixtureFluid.getMixtureTag`.
- `JeiProcessingRecipeMixin`: Create 6 `ProcessingRecipeParams` (top-level class), `SizedFluidIngredient`
  (`.ingredient() instanceof MixtureFluidIngredient`), `getSubType().getContainedMolecules(...)`.
- `CreateRecipeCategoryMixin` (mixture tooltips): Create 6's `addPotionTooltip` has the same shape
  (`Optional<FluidStack> displayed` captured at `Optional.get`); reads the Mixture components.
- Categories: `SizedFluidIngredient` (aging, centrifugation), spout `withFluids(Arrays.asList(getFluids()))`,
  holder `RecipeType` fields, 1.21 `ShapelessRecipe` ctor, map IDs via `DataComponents.MAP_ID`,
  `ResourceLocation.withDefaultNamespace`; `ArcFurnaceCategory.toBasinRecipe` is now
  `RecipeHolder<?> → RecipeHolder<BasinRecipe>` using `StandardProcessingRecipe.Builder` (ID `arc_furnace_<path>`
  as 1.20.1); music discs via `BuiltInRegistries.ITEM.getTag(Tags.Items.MUSIC_DISCS)`.
- **Runtime fixes found by the client smoke test:**
  - `ChargingRecipe.getJEISubCategory` pointed at Create's `AssemblyDeploying` (scaffolding stand-in) → JEI
    "broken recipe" on `sequenced_assembly/blacklight` (reads ingredient #1). Restored 1.20.1's
    `AssemblyChargingSubCategory`.
  - `SwissArmyKnifeToolC2SPacket` decoder skipped the always-written int → **NeoForge disconnects the player on
    join** (Forge ignored leftover bytes). Decoder now always reads it. All other packets audited: symmetric.
  - Extended inventory hotbar selection blitted 1.20.1's `textures/gui/widgets.png` (gone since 1.20.2 → black
    square) → `blitSprite(hud/hotbar_selection, …, 24, 23)` like vanilla.

### Client smoke-test runbook (headless, no window on the desktop)

1. Needs a world in `run/saves/`: `cp -r run/world run/saves/smoketest` (the GameTest server's world) and
   `run/options.txt` with `onboardAccessibility:false` (else the first-launch screen blocks quick play).
2. Temporarily add to the `client` run in `build.gradle` (**do not commit**):
   `programArguments.addAll('--quickPlaySingleplayer', 'smoketest')` (`--args` on `runClient` doesn't work — it
   replaces DevLaunch's main-class argument).
3. `xvfb-run -a -s '-screen 0 1280x720x24' ./gradlew runClient > log` — ~5 min to reach the world on llvmpipe.
   Check the log for `Starting JEI took`, `broken recipe`, `lost connection`, `[ERROR]`. Screenshot:
   `XAUTHORITY=<Xvfb -auth path> DISPLAY=:<n> import -window root shot.png` (ImageMagick installed in the toolbox).
4. Payload debugging: a throwaway mixin on `ServerboundCustomPayloadPacket.<init>` printing `payload.type().id()`
   names the packet behind a "found N bytes extra" disconnect.

### Next

1. **Azzie tests `dist/destroy-1.21.1-0.2.0.jar` in a real client** (with JEI): JEI categories render,
   molecule ingredients, mixture fluid subtypes/tooltips, recipe book slots, ghost ingredients on the Redstone
   Programmer screen, extended inventory + its JEI handler, pollution / seismology (the reworked steps 3–4).
2. Then step 8/9 per `PORTING-HANDOFF.md` (dedicated server test; datagen note: `runData` fails loot validation
   because Registrate no longer adds default drop-self loot — the committed converted 1.20.1 loot tables are used
   instead; GitHub release + pack integration runbook).

## Process

- Compile: `cd /var/home/azziee/Documents/Azziecraft/build/destroy && JAVA_HOME=/var/home/azziee/Documents/Azziecraft/build/jdk21 ./gradlew compileJava --console=plain > /var/home/azziee/Documents/Azziecraft/build/compileN.log 2>&1`
  (incremental ≈2 s; AT changes force a ~3 min NeoForm artifact rebuild).
- GameTest data check: `./gradlew runGameTestServer` (first run downloads the server jar,
  ~12 min; later runs ~1 min).
- Work in batches: fix → compile → `parse_errors.py` → repeat. Commit + push after each batch.
  Commit format: `git commit -m "<msg>" -m "Generated by Mistral Vibe.\nCo-Authored-By: Mistral Vibe <vibe@mistral.ai>"`.
- **Ask the user to test the compiled mod after every large step.**
- Session kills: `/tmp` wiped; `~/.ssh/netbox` disappears (re-copy from
  `/var/home/azziee/Documents/Azziecraft/netbox`, `chmod 600`, `-o IdentitiesOnly=yes`,
  passphrase via `SSH_ASKPASS` + `SSH_ASKPASS_REQUIRE=force`; sshd on **port 22**, user `azziee`);
  replays reset exec bits (`chmod -R +x build/jdk21/bin/`, `chmod +x gradlew`);
  `build/stray/com_src_main_java` is a stale duplicate — never restore into `src/`.

## History

| Commit | Contents |
|---|---|
| `d9fc4668` | (Maddie) Runtime fixes, GameTest data check, built jar |
| `448717a7` | Recipe load fixes: `SingleFluidRecipe` duration default gated on `canSpecifyDuration()`; borax biomes → `#destroy:borax_distillation` tag. GameTest server: all data loads clean, tests pass. |
| `159ae373` | Step 6 WIP: JEI compat restored from 1.20.1 (54 files), 8 mixins registered, mechanical renames + fix batch 1 + `super.draw` removal + holder-based RecipeTypes. Errors: 148 → 69, all remaining in the JEI files. + this doc. |
| `167d6945` | Step 6 compiles: holder helpers, plugin/mixins on NeoForge + Create 6 types, categories. |
| *(this commit)* | Client smoke test fixes: charging JEI sub-category, Swiss Army Knife packet decode, hotbar selection sprite; jar rebuilt; this doc. |
