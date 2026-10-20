package petrolpark.mc.destroy.content.product.alcohol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.mobeffect.DestroyMobEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@MoveToPetrolparkLibrary
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class HangoverMobEffect extends DestroyMobEffect {
    
    public HangoverMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x59390B);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "31875c8a-f500-477c-ac52-70355c6adc12", (double)-0.10F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    };

    /**
     * As in 1.20.1, milk does not cure this (its curative items did not include milk), but a Totem of Undying still clears it.
     */
    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.add(EffectCures.PROTECTED_BY_TOTEM);
    };

    @SubscribeEvent
    public static void onPlayerHearsSound(PlayLevelSoundEvent.AtPosition event) {
        if (event.getOriginalVolume() < DestroyAllConfigs.SERVER.substances.soundSourceThresholds.get(event.getSource()).getF()) return;
        Vec3 pos = event.getPosition();
        float radius = DestroyAllConfigs.SERVER.substances.hangoverNoiseTriggerRadius.getF();
        List<Entity> nearbyEntities = event.getLevel().getEntities(null, new AABB(pos.add(new Vec3(-radius,-radius,-radius)), pos.add(new Vec3(radius, radius, radius))));
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hasEffect(DestroyMobEffects.HANGOVER)) {
                    livingEntity.hurt(DestroyDamageSources.headache(livingEntity.level()), DestroyAllConfigs.SERVER.substances.soundSourceDamage.get(event.getSource()).getF());
                };
            };
        }; 
    };
};
