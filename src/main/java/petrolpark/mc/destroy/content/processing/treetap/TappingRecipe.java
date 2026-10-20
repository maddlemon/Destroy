package petrolpark.mc.destroy.content.processing.treetap;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class TappingRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {

    public static int recipeId = 0;

    public TappingRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.TAPPING, params);
    };

    public static TappingRecipe create(BlockTapping tapping) {
        return new AdvancedProcessingRecipe.Builder<>(TappingRecipe::new, Destroy.asResource("tapping_"+recipeId++))
            .require(Ingredient.of(tapping.displayItems.toArray(new ItemStack[tapping.displayItems.size()])))
            .withFluidOutputs(tapping.result.copy())
            .build();
    };

    @Override
    public boolean matches(RecipeWrapper pContainer, Level pLevel) {
        return false;
    };

    @Override
    protected int getMaxInputCount() {
        return 1;
    };

    @Override
    protected int getMaxOutputCount() {
        return 0;
    };

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    };
    
};
