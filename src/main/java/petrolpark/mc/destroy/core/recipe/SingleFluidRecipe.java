package petrolpark.mc.destroy.core.recipe;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;

public abstract class SingleFluidRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {

    public SingleFluidRecipe(IRecipeTypeInfo typeInfo, AdvancedProcessingRecipeParams params) {
        super(typeInfo, params);
        // Only default the duration for types that use it; Create 6 validates `processingDuration > 0`
        // against canSpecifyDuration() at load, and types like Distillation never read the duration
        if (processingDuration <= 0 && canSpecifyDuration()) processingDuration = 20;
    };

    public SizedFluidIngredient getRequiredFluid() {
        if (fluidIngredients.isEmpty()) {
            // 1.21 recipes don't know their own id; the recipe type still identifies the problem
            throw new IllegalStateException(this.getRecipeTypeName() + " Recipe has no fluid ingredient!");
        };
        return fluidIngredients.get(0);
    };

    @Override
    public boolean matches(RecipeWrapper pContainer, Level level) {
        return true;
    };

    @Override
    protected int getMaxInputCount() {
        return 0;
    };

    @Override
    protected int getMaxOutputCount() {
        return 0;
    };

    @Override
    protected final int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    };

    @Override
    protected boolean canSpecifyDuration() {
		return true;
	};

    /**
     * Used in error message
     * @return Name of Recipe Type (e.g. "Centrifugation")
     */
    public abstract String getRecipeTypeName();
}
