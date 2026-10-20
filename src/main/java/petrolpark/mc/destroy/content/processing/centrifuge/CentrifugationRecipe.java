package petrolpark.mc.destroy.content.processing.centrifuge;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Allowed biomes come from {@code AdvancedProcessingRecipe} ({@code "biomes"} in JSON).
 */
public class CentrifugationRecipe extends SingleFluidRecipe {

    public CentrifugationRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.CENTRIFUGATION, params);
    };

    public FluidStack getDenseOutputFluid() {
        checkForValidOutputs();
        return fluidResults.get(0);
    };

    public FluidStack getLightOutputFluid() {
        checkForValidOutputs();
        return fluidResults.get(1);
    };

    private void checkForValidOutputs() {
        if (fluidResults.isEmpty() || fluidResults.size() != 2) throw new IllegalStateException("Centrifugation Recipe contains the wrong number of output fluids.");
    };

    @Override
    public String getRecipeTypeName() {
        return "Centrifugation";
    }

    
}
