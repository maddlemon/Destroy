package petrolpark.mc.destroy.core.explosion;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import petrolpark.mc.destroy.DestroyRecipeTypes;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * These Recipes are display only. Creating an Obliteration Recipe will not add it to the game -
 * the loot table of the Block must also be changed.
 */
public class ObliterationRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {

    public ObliterationRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.OBLITERATION, params);
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
        return 1;
    };
    
};
