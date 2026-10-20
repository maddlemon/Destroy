package petrolpark.mc.destroy.content.processing.ageing;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class AgeingRecipe extends SingleFluidRecipe {

    public AgeingRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.AGING, params);
    };

    @Override
    protected int getMaxInputCount() {
        return 2;
    };

    @Override
    protected int getMaxOutputCount() {
        return 0;
    };

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    };

    @Override
    public boolean matches(RecipeWrapper pContainer, Level level) {
        return false;
    };

    @Override
    public String getRecipeTypeName() {
        return "aging";
    };
    
}
