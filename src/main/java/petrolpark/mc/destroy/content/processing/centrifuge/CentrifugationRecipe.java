package petrolpark.mc.destroy.content.processing.centrifuge;

import java.util.Set;
import java.util.HashSet;

import com.ibm.icu.impl.locale.XCldrStub.ImmutableSet;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;
import petrolpark.mc.library.core.data.recipe.IBiomeSpecificRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.neoforged.neoforge.fluids.FluidStack;

public class CentrifugationRecipe extends SingleFluidRecipe implements IBiomeSpecificProcessingRecipe {
    
    private Set<BiomeValue> biomes = new HashSet<>();

    public CentrifugationRecipe(ProcessingRecipeParams params) {
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
        if (fluidResults.isEmpty() || fluidResults.size() != 2) throw new IllegalStateException("Centrifugation Recipe " + id + " contains the wrong number of output fluids.");
    };

    @Override
    public String getRecipeTypeName() {
        return "Centrifugation";
    }

    @Override
    public void setAllowedBiomes(Set<BiomeValue> biomes) {
        biomes = ImmutableSet.copyOf(biomes);
    };

    @Override
    public Set<BiomeValue> getAllowedBiomes() {
        return biomes;
    };
    
}
