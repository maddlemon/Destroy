package petrolpark.mc.destroy.compat.jei.animation;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.addFluidSlot;

import java.util.ArrayList;
import java.util.List;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.util.DestroyLang;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.RefrigerantDummyFluidIngredient;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class HeatConditionRenderer {

    private static RefrigerantDummyFluidIngredient refrigerantIngredient = new RefrigerantDummyFluidIngredient();

    public static void drawHeatConditionName(Font font, GuiGraphics graphics, int x, int y, HeatCondition requiredHeat) {
        MutableComponent name = Component.empty();
        if ("COOLED".equals(requiredHeat.name())) { // Scuffed but okay keep your opinions to yourself
            name = DestroyLang.translate(requiredHeat.getTranslationKey()).component();
        } else {
            name = CreateLang.translate(requiredHeat.getTranslationKey()).component();
        };
        graphics.drawString(font, name, x, y, requiredHeat.getColor(), false); // This is equivalent of the line being overwritten
    };

    /**
     * Render the Blaze Burner or Cooler, and superheating treat if necessary.
     */
    public static void addHeatConditionSlots(IRecipeLayoutBuilder builder, int x, int y, HeatCondition requiredHeat) {
        List<ItemStack> blazeTreatStacks = new ArrayList<>();
        BuiltInRegistries.ITEM.getTag(AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.tag).ifPresent(holders -> {
            holders.forEach(holder -> blazeTreatStacks.add(new ItemStack(holder.value())));
        });
        
        if ("COOLED".equals(requiredHeat.name())) {
            builder
                .addSlot(RecipeIngredientRole.CATALYST, x, y)
                .addItemStack(DestroyBlocks.COOLER.asStack());
            addFluidSlot(builder, x + 19, y, new SizedFluidIngredient(refrigerantIngredient, 1000));
        } else if (requiredHeat != HeatCondition.NONE) { // This one is copied right from Create; it renders the Blaze Burner
            builder
                .addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                .addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        };
        if (requiredHeat == HeatCondition.SUPERHEATED) { // Used to render all possible Blaze 'treats' rather than just the Blaze Cake
            builder
                .addSlot(RecipeIngredientRole.CATALYST, x + 19, y)
                .addItemStacks(blazeTreatStacks);
        };
    };
};
