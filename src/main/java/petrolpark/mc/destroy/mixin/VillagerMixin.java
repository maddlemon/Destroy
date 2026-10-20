package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.mixin.accessor.AbstractVillagerAccessor;
import petrolpark.mc.destroy.mixin.accessor.AgeableMobAccessor;
import petrolpark.mc.destroy.mixin.accessor.VillagerAccessor;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Villager.class)
public class VillagerMixin {
    
    /**
     * Overwritten but mostly copied from {@link net.minecraft.world.entity.npc.Villager#wantsToPickUp Villager}.
     * This allows baby Villagers to pick up toys.
     */
    @Overwrite
    public boolean wantsToPickUp(ItemStack pStack) {
        // All this is copied from the Minecraft source code
        Item item = pStack.getItem();
        return (Villager.WANTED_ITEMS.contains(item) 
            || ((VillagerAccessor)this).invokeGetVillagerData().getProfession().requestedItems().contains(item)) && ((AbstractVillagerAccessor)this).invokeGetInventory().canAddItem(pStack)
        //
            || ((AgeableMobAccessor)this).invokeIsBaby() && DestroyItems.BUCKET_AND_SPADE.isIn(pStack);
    };
};
