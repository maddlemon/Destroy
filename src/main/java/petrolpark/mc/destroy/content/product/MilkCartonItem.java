package petrolpark.mc.destroy.content.product;

import java.util.Set;

import petrolpark.mc.destroy.core.item.DrinkItem;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.EffectCures;

public class MilkCartonItem extends DrinkItem {

    public MilkCartonItem(Properties properties) {
        super(properties);
    };

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {

        Set<MobEffectInstance> effects = Set.copyOf(livingEntity.getActiveEffects());
        for (MobEffectInstance effect : effects) {
            if (effect.getCures().contains(EffectCures.MILK) && effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) livingEntity.removeEffect(effect.getEffect());
        };

        return super.finishUsingItem(stack, level, livingEntity);
    };
    
};
