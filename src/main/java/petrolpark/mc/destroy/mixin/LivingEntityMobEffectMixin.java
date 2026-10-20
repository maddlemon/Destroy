package petrolpark.mc.destroy.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.core.mobeffect.DestroyMobEffect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Calls 1.20.1's entity-aware {@link DestroyMobEffect#addAttributeModifiers(LivingEntity, net.minecraft.world.entity.ai.attributes.AttributeMap, int)}
 * and {@link DestroyMobEffect#removeAttributeModifiers(LivingEntity, net.minecraft.world.entity.ai.attributes.AttributeMap, int) remove} hooks
 * from the same places 1.20.1 called them, which in 1.21 only call the entity-less versions.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMobEffectMixin {

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void destroy$onEffectAdded(MobEffectInstance effectInstance, @Nullable Entity entity, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (!livingEntity.level().isClientSide() && effectInstance.getEffect().value() instanceof DestroyMobEffect effect)
            effect.addAttributeModifiers(livingEntity, livingEntity.getAttributes(), effectInstance.getAmplifier());
    };

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void destroy$onEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (forced && !livingEntity.level().isClientSide() && effectInstance.getEffect().value() instanceof DestroyMobEffect effect) {
            effect.removeAttributeModifiers(livingEntity, livingEntity.getAttributes(), effectInstance.getAmplifier());
            effect.addAttributeModifiers(livingEntity, livingEntity.getAttributes(), effectInstance.getAmplifier());
        };
    };

    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void destroy$onEffectRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        if (!livingEntity.level().isClientSide() && effectInstance.getEffect().value() instanceof DestroyMobEffect effect)
            effect.removeAttributeModifiers(livingEntity, livingEntity.getAttributes(), effectInstance.getAmplifier());
    };
};
