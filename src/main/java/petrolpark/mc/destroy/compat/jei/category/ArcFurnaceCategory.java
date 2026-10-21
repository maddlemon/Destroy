package petrolpark.mc.destroy.compat.jei.category;

import petrolpark.mc.destroy.compat.jei.animation.AnimatedDynamo;
import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ArcFurnaceCategory extends BasinCategory {

    private final AnimatedDynamo dynamo;

    protected final RecipeType<RecipeHolder<BasinRecipe>> type;

    public ArcFurnaceCategory(Info<BasinRecipe> info) {
        super(info, false);
        dynamo = new AnimatedDynamo(true, true);
        type = info.recipeType();
    };

    public static RecipeHolder<BasinRecipe> toBasinRecipe(RecipeHolder<?> holder) {
        Minecraft mc = Minecraft.getInstance();
        AbstractCookingRecipe recipe = (AbstractCookingRecipe)holder.value(); // Only used for Blasting and Smelting Recipes
        ResourceLocation id = holder.id().withPath(path -> "arc_furnace_"+path);
        return new RecipeHolder<>(id, new StandardProcessingRecipe.Builder<>(BasinRecipe::new, id)
            .withItemIngredients(recipe.getIngredients())
            .output(recipe.getResultItem(mc.level.registryAccess()))
            .duration(recipe.getCookingTime())
            .build());
    };

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        dynamo.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    };
    
};
