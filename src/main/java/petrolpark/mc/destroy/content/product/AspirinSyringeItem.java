package petrolpark.mc.destroy.content.product;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.content.tool.syringe.SyringeItem;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AspirinSyringeItem extends SyringeItem {

    public AspirinSyringeItem(Properties properties) {
        super(properties);
    };

    @Override
    public void onInject(ItemStack itemStack, Level level, LivingEntity target) {
        target.heal(DestroyAllConfigs.SERVER.substances.aspirinHeal.getF());
        if (!target.removeEffect(DestroyMobEffects.HANGOVER.get())) return;
        if (target instanceof Player player) {
            DestroyAdvancementTrigger.CURE_HANGOVER.award(level, player);
        };
    };
    
};
