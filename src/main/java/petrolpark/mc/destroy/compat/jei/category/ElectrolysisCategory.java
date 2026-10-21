package petrolpark.mc.destroy.compat.jei.category;

import net.neoforged.neoforge.common.Tags;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.compat.jei.DestroyJEI;
import petrolpark.mc.destroy.compat.jei.animation.AnimatedDynamo;
import petrolpark.mc.destroy.content.processing.discstamping.DiscElectroplatingRecipe;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ElectrolysisCategory extends BasinCategory {

    private final AnimatedDynamo dynamo;

    protected final RecipeType<RecipeHolder<BasinRecipe>> type;

    public ElectrolysisCategory(Info<BasinRecipe> info) {
        super(info, false);
        dynamo = new AnimatedDynamo(true, false);
        type = info.recipeType();
    };

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        dynamo.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    };

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        super.registerRecipes(registration);
        CreateJEI.<DiscElectroplatingRecipe>consumeTypedRecipes(holder -> {
            if (holder.value() instanceof DiscElectroplatingRecipe recipe && recipe.original) registration.addRecipes(type, BuiltInRegistries.ITEM.getTag(Tags.Items.MUSIC_DISCS).stream().flatMap(HolderSet.Named::stream).map(Holder::value).filter(item -> !item.equals(DestroyItems.BLANK_MUSIC_DISC.get())).map(
                item -> DestroyJEI.holderOf(Destroy.asResource("disc_electroplating_"+Item.getId(item)), recipe.copyWithDisc(new ItemStack(item))) // ID as in 1.20.1
            ).toList());
        }, DestroyRecipeTypes.DISC_ELECTROPLATING.getType());
    };
    
};
