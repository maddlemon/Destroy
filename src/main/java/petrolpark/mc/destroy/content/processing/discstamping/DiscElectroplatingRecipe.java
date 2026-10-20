package petrolpark.mc.destroy.content.processing.discstamping;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class DiscElectroplatingRecipe extends BasinRecipe {

    public final boolean original;

    public DiscElectroplatingRecipe(ProcessingRecipeParams params) {
        this(params, true);
    };

    private DiscElectroplatingRecipe(ProcessingRecipeParams params, boolean original) {
        super(DestroyRecipeTypes.DISC_ELECTROPLATING, params);
        this.original = original;
    };

    public BasinRecipe copyWithDisc(ItemStack discStack) {

        ProcessingRecipeBuilder<DiscElectroplatingRecipe> builder = new ProcessingRecipeBuilder<DiscElectroplatingRecipe>(params -> new DiscElectroplatingRecipe(params, false), Destroy.asResource("disc_electroplating_"+Item.getId(discStack.getItem())))
            .requiresHeat(requiredHeat)
            .duration(processingDuration)
            .require(Ingredient.of(discStack))
            .output(DiscStamperItem.of(discStack));

        ingredients.forEach(builder::require);
        fluidIngredients.forEach(builder::require);
        results.forEach(builder::output);
        fluidResults.forEach(builder::output);

        return builder.build();
    };
};
