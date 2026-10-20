package petrolpark.mc.destroy.content.product.fireretardant;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class FlameRetardantApplicationRecipe extends SingleFluidRecipe {

    public FlameRetardantApplicationRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION, params);
    };

    @Override
    public boolean matches(RecipeWrapper container, Level level) {
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
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    @Override
    public String getRecipeTypeName() {
        return "flame retardant application";
    };


    
};
