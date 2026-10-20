package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.HolderLookup;
import javax.annotation.Nullable;

import net.createmod.catnip.data.Iterate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class FillMixedExplosiveItemRecipe extends CustomRecipe {

    public static final RecipeSerializer<FillMixedExplosiveItemRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(FillMixedExplosiveItemRecipe::new);

    public FillMixedExplosiveItemRecipe(CraftingBookCategory category) {
        super(category);
    };

    @Override
    public boolean matches(CraftingInput container, Level level) {
        // 1.20.1 passed null here; 1.21 needs registries to read the explosive inventory off the Item
        return assemble(container, level.registryAccess()) != ItemStack.EMPTY;
    };

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registryAccess) {
        boolean anyExplosiveFound = false;
        ItemStack mixItem = ItemStack.EMPTY;
        MixedExplosiveInventory inv = null;
        for (boolean findMixItem : Iterate.trueAndFalse) {
            for (int slot = 0; slot < container.size(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.getItem() instanceof IMixedExplosiveItem customMixItem) { 
                    if (findMixItem) { // If we're looking for a mix container and we've found one
                        if (inv != null) return ItemStack.EMPTY; // Only one mix container allowed
                        else {
                            mixItem = stack;
                            inv = customMixItem.getExplosiveInventory(stack, registryAccess);
                        };
                    };
                } else if (MixedExplosiveInventory.canBeAdded(stack)) {
                    anyExplosiveFound = true;
                    if (!findMixItem && inv != null && ItemHandlerHelper.insertItem(inv, stack, false) != ItemStack.EMPTY) return ItemStack.EMPTY; 
                } else if (!stack.isEmpty()) {
                    return ItemStack.EMPTY;
                };
            };
        };
        if (!anyExplosiveFound || mixItem.isEmpty()) return ItemStack.EMPTY; // If a mix Item or explosive was never found
        ItemStack result = mixItem.copy();
        if (result.getItem() instanceof IMixedExplosiveItem customMixItem) customMixItem.setExplosiveInventory(result, inv, registryAccess); // Check should never fail
        return result;
    };

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    };

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    };
    
};
