package petrolpark.mc.destroy.content.product;

import java.util.function.Supplier;

import petrolpark.mc.destroy.core.item.DecayingItem;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import petrolpark.mc.library.core.world.item.decay.ItemDecay;
import petrolpark.mc.library.registry.PetrolparkDataComponentTypes;

public class OxidizingItem extends DecayingItem {

    public OxidizingItem(Properties properties, Supplier<ItemStack> decayProduct, Supplier<ConfigBase.ConfigInt> lifetime, int defaultLifetime) {
        super(properties, decayProduct, lifetime, defaultLifetime, "item.destroy.oxidizing_item.remaining");
    };

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        checkForWater(stack, entity, isSelected);
    };

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        checkForWater(stack, entity, true);
        return super.onEntityItemUpdate(stack, entity);
    };

    public void checkForWater(ItemStack stack, Entity entity, boolean rainSensitive) {
        if (entity.isInWaterOrBubble() || (entity.isInWaterRainOrBubble() && (rainSensitive || (entity instanceof LivingEntity livingEntity && livingEntity.getOffhandItem() == stack)))) {
            // Instantly rust: no time remaining, whether or not decay had started (what 1.20.1's extendLifetime by minus the remaining time did)
            stack.set(PetrolparkDataComponentTypes.DECAY_START_TIME, ItemDecay.getGameTime() - ItemDecay.getLifetimeOrNone(stack));
        };
    };

};
