package petrolpark.mc.destroy.content.processing.centrifuge.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.util.DestroyFluidIngredientHelper;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Mixing recipes which substitute Destroy's potion-modifier Fluids for brewing ingredients.
 * <p>1.21: brewing is per-level ({@link Level#potionBrewing()}), so like Create's {@code PotionMixingRecipes} these are generated
 * from the first Level that asks and then cached, instead of at class load.</p>
 */
public class PotionFluidMixingRecipes {

    static List<Item> VANILLA_CONTAINERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

    /**
     * The Fluid equivalents of Potion Items, in order of priority for which one gets made when Centrifuging
     */
    static Map<Item, FluidStack> FLUID_EQUIVALENTS = createFluidEquivalents();

    private static List<MixingRecipe> RECIPES = null;

    public static Map<Item, FluidStack> createFluidEquivalents() {
        return Map.of(
            Items.DRAGON_BREATH, new FluidStack((Fluid) DestroyFluids.LINGERING_POTION.getSource(), 100),
            Items.GUNPOWDER, new FluidStack((Fluid) DestroyFluids.SPLASH_POTION.getSource(), 100),
            Items.REDSTONE, new FluidStack((Fluid) DestroyFluids.LONG_POTION.getSource(), 100),
            Items.GLOWSTONE_DUST, new FluidStack((Fluid) DestroyFluids.STRONG_POTION.getSource(), 100),
            Items.FERMENTED_SPIDER_EYE, new FluidStack((Fluid) DestroyFluids.CORRUPTING_POTION.getSource(), 100)
        );
    };

    /**
     * All potion Fluid Mixing recipes (was the static {@code ALL} in 1.20.1).
     */
    public static List<MixingRecipe> getRecipes(Level level) {
        if (RECIPES == null) RECIPES = createRecipes(level);
        return RECIPES;
    };

    private static List<MixingRecipe> createRecipes(Level level) {
		List<MixingRecipe> mixingRecipes = new ArrayList<>();
        PotionBrewing potionBrewing = level.potionBrewing();

		int recipeIndex = 0;

		for (Item container : VANILLA_CONTAINERS) {
			BottleType bottleType = PotionFluidHandler.bottleTypeFromItem(container);
			for (PotionBrewing.Mix<Potion> mix : potionBrewing.potionMixes) {
                for (Entry<Item, FluidStack> entry : FLUID_EQUIVALENTS.entrySet()) {
                    if (mix.ingredient().test(new ItemStack(entry.getKey()))) {
                        FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.from()), bottleType, 1000);
                        FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(mix.to()), bottleType, 1000);
                        mixingRecipes.add(createRecipe("potion_mixing_with_fluid_" + recipeIndex++, entry.getValue(), fromFluid, toFluid));
                    };
                };
			};
		};

		for (PotionBrewing.Mix<Item> mix : potionBrewing.containerMixes) {
			Item from = mix.from().value();
			if (!VANILLA_CONTAINERS.contains(from)) continue;

			Item to = mix.to().value();
			BottleType fromBottleType = PotionFluidHandler.bottleTypeFromItem(from);
			BottleType toBottleType = PotionFluidHandler.bottleTypeFromItem(to);

            for (Entry<Item, FluidStack> entry : FLUID_EQUIVALENTS.entrySet()) {
                if (mix.ingredient().test(new ItemStack(entry.getKey()))) {
                    for (Holder<Potion> potion : level.registryAccess().registryOrThrow(Registries.POTION).holders().toList()) {
                        FluidStack fromFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), fromBottleType, 1000);
                        FluidStack toFluid = PotionFluidHandler.getFluidFromPotion(new PotionContents(potion), toBottleType, 1000);

                        mixingRecipes.add(createRecipe("potion_mixing_with_fluid_" + recipeIndex++, entry.getValue(), fromFluid, toFluid));
                    };
                };
            };
		};

		for (IBrewingRecipe recipe : potionBrewing.getRecipes()) {
			if (recipe instanceof BrewingRecipe brewingRecipe) {
				ItemStack output = brewingRecipe.getOutput();
				if (!VANILLA_CONTAINERS.contains(output.getItem())) continue;

				Ingredient input = brewingRecipe.getInput();
				Ingredient ingredient = brewingRecipe.getIngredient();
				FluidStack outputFluid = null;

                for (Item item : VANILLA_CONTAINERS) {
                    if (input.test(new ItemStack(item))) {
                        for (Entry<Item, FluidStack> entry : FLUID_EQUIVALENTS.entrySet()) {
                            ItemStack stack = new ItemStack(entry.getKey());
                            if (ingredient.test(stack)) {
                                FluidStack fromFluid = PotionFluidHandler.getFluidFromPotionItem(stack);
                                if (outputFluid == null) outputFluid = PotionFluidHandler.getFluidFromPotionItem(output);

                                mixingRecipes.add(createRecipe("potion_mixing_with_fluid_" + recipeIndex++, entry.getValue(), fromFluid, outputFluid));
                            };
                        };
                    };
                };
			};
		};

		return mixingRecipes;
	};

	private static MixingRecipe createRecipe(String id, FluidStack itemSubstitute, FluidStack fromFluid, FluidStack toFluid) {
		return new StandardProcessingRecipe.Builder<>(MixingRecipe::new, Destroy.asResource(id))
                .require(DestroyFluidIngredientHelper.fromFluidStack(itemSubstitute))
				.require(DestroyFluidIngredientHelper.fromFluidStack(fromFluid))
				.output(toFluid)
				.requiresHeat(HeatCondition.HEATED)
				.build();
	};
};
