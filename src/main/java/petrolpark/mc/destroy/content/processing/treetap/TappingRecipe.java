package petrolpark.mc.destroy.content.processing.treetap;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class TappingRecipe extends ProcessingRecipe<RecipeWrapper> {

    public static int recipeId = 0;

    public TappingRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.TAPPING, params);
    };

    public static TappingRecipe create(BlockTapping tapping) {
        return new ProcessingRecipeBuilder<>(TappingRecipe::new, Destroy.asResource("tapping_"+recipeId++))
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
