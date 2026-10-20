package petrolpark.mc.destroy.content.processing.centrifuge.potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import net.createmod.catnip.data.Pair;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyPotions;
import petrolpark.mc.destroy.content.processing.centrifuge.CentrifugationRecipe;
import petrolpark.mc.destroy.util.DestroyFluidIngredientHelper;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Centrifugation recipes which undo brewing.
 * <p>1.21: brewing is per-level ({@link Level#potionBrewing()}), so these are generated from the first Level that asks and then
 * cached (like Create's {@code PotionMixingRecipes}), instead of at class load. Keyed by potion Holder and Bottle Type.</p>
 */
public class PotionSeparationRecipes {

    private static Map<Pair<Holder<Potion>, BottleType>, CentrifugationRecipe> RECIPES = null;

    /**
     * All potion separation recipes (was the static {@code ALL} in 1.20.1).
     */
    public static Map<Pair<Holder<Potion>, BottleType>, CentrifugationRecipe> getRecipes(Level level) {
        if (RECIPES == null) RECIPES = createSeparationRecipes(level);
        return RECIPES;
    };

    private static Map<Pair<Holder<Potion>, BottleType>, CentrifugationRecipe> createSeparationRecipes(Level level) {
        Map<Pair<Holder<Potion>, BottleType>, CentrifugationRecipe> recipes = new HashMap<>();
        PotionBrewing potionBrewing = level.potionBrewing();
        List<Holder.Reference<Potion>> allPotions = level.registryAccess().registryOrThrow(Registries.POTION).holders().toList();

        int recipeIndex = 0;

        // Highest priority - lingering and splash potions
        for (PotionBrewing.Mix<Item> mix : potionBrewing.containerMixes) {
			Item from = mix.from().value();
			if (!PotionFluidMixingRecipes.VANILLA_CONTAINERS.contains(from)) continue;

			Item to = mix.to().value();
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to); // The potion the original recipe produces, and so the one which the separation will take as input

            for (Entry<Item, FluidStack> entry : PotionFluidMixingRecipes.FLUID_EQUIVALENTS.entrySet()) {
                if (mix.ingredient().test(new ItemStack(entry.getKey()))) {
                    for (Holder<Potion> potion : allPotions) {
                        Pair<Holder<Potion>, BottleType> potionAndType = Pair.of(potion, toBottleType);

                        FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), fromBottleType, 1000);
                        FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), toBottleType, 1000);

                        recipes.putIfAbsent(potionAndType, createRecipe("potion_separation_" + recipeIndex++, toFluid, entry.getValue(), fromFluid));
                    };
                };
            };
		};

        // Next highest priority - strong, long and fermented spider eye potions
        for (Item container : PotionFluidMixingRecipes.VANILLA_CONTAINERS) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : potionBrewing.potionMixes) {
                for (Entry<Item, FluidStack> entry : PotionFluidMixingRecipes.FLUID_EQUIVALENTS.entrySet()) {
                    Holder<Potion> toPotion = mix.to();
                    Pair<Holder<Potion>, BottleType> potionAndType = Pair.of(toPotion, bottleType);
                    if (mix.ingredient().test(new ItemStack(entry.getKey())) && !recipes.containsKey(potionAndType)) {

                        FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.from()), bottleType, 1000);
                        FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(toPotion), bottleType, 1000); // The potion the original recipe produces, and so the one which the separation will take as input

                        if (!fromFluid.getFluid().isSame(Fluids.WATER)) recipes.putIfAbsent(potionAndType, createRecipe("potion_separation_" + recipeIndex++, toFluid, entry.getValue(), fromFluid));
                    };
                };
			};
		};

        // Lowest priority - split one potion with multiple effects into multiple potions
        for (Holder<Potion> potion : allPotions) {
            List<MobEffectInstance> effects = potion.value().getEffects();
            if (effects.size() > 1) {
                List<MobEffectInstance> firstEffects = new ArrayList<>();
                List<MobEffectInstance> secondEffects = new ArrayList<>();
                int i = 0;
                for (MobEffectInstance effect : effects) {
                    (i < effects.size() / 2 ? firstEffects : secondEffects).add(effect);
                    i++;
                };
                addEachContainer: for (Item item : PotionFluidMixingRecipes.VANILLA_CONTAINERS) {
                    BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(item); // The potion the original recipe produces, and so the one which the separation will take as input

                    Pair<Holder<Potion>, BottleType> potionAndType = Pair.of(potion, bottleType);

                    if (recipes.containsKey(potionAndType)) continue addEachContainer;

                    // 1.20.1 appended the effects to a 'Unique' potion Fluid's NBT; in 1.21 they're the custom effects of its Potion Contents
                    FluidStack lightOutput = PotionFluidHandler.getFluidFromPotion(new PotionContents(Optional.of(DestroyPotions.UNIQUE), Optional.empty(), firstEffects), bottleType, 1000);
                    FluidStack denseOutput = PotionFluidHandler.getFluidFromPotion(new PotionContents(Optional.of(DestroyPotions.UNIQUE), Optional.empty(), secondEffects), bottleType, 1000);

                    recipes.put(potionAndType, createRecipe("potion_separation_" + recipeIndex++, PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), bottleType, 1000), lightOutput, denseOutput));
                };
            };
        };

        return recipes;
    };

    private static CentrifugationRecipe createRecipe(String name, FluidStack from, FluidStack light, FluidStack dense) {
        return new AdvancedProcessingRecipe.Builder<>(CentrifugationRecipe::new, Destroy.asResource(name))
            .require(DestroyFluidIngredientHelper.fromFluidStack(from))
            .duration(200)
            .output(light)
            .output(dense)
            .build();
    };
};
