package petrolpark.mc.destroy.content.product.alcohol;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.mobeffect.UncurableMobEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@MoveToPetrolparkLibrary
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class InebriationMobEffect extends UncurableMobEffect {
    
    public InebriationMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE88010);
    };

    @SuppressWarnings("null")
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        int pDuration = livingEntity.getEffect(DestroyMobEffects.INEBRIATION.get()).getDuration(); // This is the bit it says is null
        if (!livingEntity.level().isClientSide()) {
            if (amplifier >= 3) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, (amplifier - 2), true, false, false));
            };
            if (amplifier >= 6) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, (amplifier - 6), true, false, false));
            };
            if (amplifier >= 9) {
                if (pDuration % Math.round(250 / amplifier) == 0) {
                    livingEntity.hurt(DestroyDamageSources.alcohol(livingEntity.level()), 1f);
                };
                if (livingEntity instanceof Player player) {
                    DestroyAdvancementTrigger.VERY_DRUNK.award(player.level(), player);
                };
            };
        };
        super.applyEffectTick(livingEntity, amplifier);
    };

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    };

    // Give hangovers when waking up
    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        for (Player player : event.getLevel().players()) {
            if (!player.isSleeping()) continue;
            MobEffectInstance effect = player.getEffect(DestroyMobEffects.INEBRIATION.get());
            if (effect != null) {
                player.addEffect(new MobEffectInstance(DestroyMobEffects.HANGOVER.get(), DestroyAllConfigs.SERVER.substances.hangoverDuration.get() * (effect.getAmplifier() + 1)));
                player.removeEffect(DestroyMobEffects.INEBRIATION.get());
                DestroyAdvancementTrigger.HANGOVER.award(player.level(), player);
            };
        };
    };
};
