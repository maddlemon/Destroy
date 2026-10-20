package petrolpark.mc.destroy.core.explosion;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * These Recipes are display only. Creating an Obliteration Recipe will not add it to the game -
 * the loot table of the Block must also be changed.
 */
public class ObliterationRecipe extends ProcessingRecipe<RecipeWrapper> {

    public ObliterationRecipe(ProcessingRecipeParams params) {
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
