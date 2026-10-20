package petrolpark.mc.destroy.content.processing.sieve;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import petrolpark.mc.destroy.DestroyRecipeTypes;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * First-time-lucky comes from {@code AdvancedProcessingRecipe} ({@code "first_time_lucky_key"} in JSON; 1.20.1's
 * {@code "lucky_first_time": true} becomes the recipe's own id as the key). Roll with {@code rollLuckyResults}.
 */
public class SievingRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {
    
    public SievingRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.SIEVING, params);
    };

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        if (inv.isEmpty()) return false;
		return ingredients.get(0).test(inv.getItem(0));
    };

    @Override
    protected int getMaxInputCount() {
        return 1;
    };

    @Override
    protected int getMaxOutputCount() {
        return 16;
    };

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    };

    
};
