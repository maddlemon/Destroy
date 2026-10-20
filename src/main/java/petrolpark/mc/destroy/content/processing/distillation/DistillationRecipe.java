package petrolpark.mc.destroy.content.processing.distillation;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;

/**
 * Allowed biomes come from {@code AdvancedProcessingRecipe} ({@code "biomes"} in JSON).
 */
public class DistillationRecipe extends SingleFluidRecipe {

    public DistillationRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.DISTILLATION, params);
    };

    /**
     * The number of fractions this Recipe produces
     */
    public int getFractions() {
        return getFluidResults().size();
    };

    @Override
    protected int getMaxFluidOutputCount() {
        return 7;
    };

    @Override
    protected boolean canRequireHeat() {
        return true;
    };

    @Override
    protected boolean canSpecifyDuration() {
        return false;
    };

    @Override
    public String getRecipeTypeName() {
        return "distillation";
    }

    
}
