package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;
import petrolpark.mc.destroy.core.pollution.Pollution.PollutionType;
import petrolpark.mc.destroy.mixin.accessor.AbstractVillagerAccessor;
import petrolpark.mc.destroy.mixin.accessor.AgeableMobAccessor;
import petrolpark.mc.destroy.mixin.accessor.VillagerAccessor;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

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

    /**
     * Make trades more expensive with higher levels of Smog.
     */
    @Inject(method = "updateSpecialPrices", at = @At(value = "RETURN"))
    public void inUpdateSpecialPrices(Player player, CallbackInfo ci) {
        if (!PollutionHelper.pollutionEnabled() || !DestroyAllConfigs.SERVER.pollution.villagersIncreasePrices.get()) return;
        Villager thisVillager = (Villager)(Object)this;
        for (MerchantOffer trade : thisVillager.getOffers()) {
            int change = (int)(50d * (double)PollutionHelper.getPollution(thisVillager.level(), thisVillager.getOnPos(), PollutionType.SMOG) / (double)PollutionType.SMOG.max);
            trade.addToSpecialPriceDiff(change);
        };
    };
};
