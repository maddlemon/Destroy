package petrolpark.mc.destroy.content.processing.dynamo;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

public class ElectrolysisRecipe extends BasinRecipe {

    public ElectrolysisRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.ELECTROLYSIS, params);
        if (processingDuration == 0) processingDuration = 200;
    };
    
};
