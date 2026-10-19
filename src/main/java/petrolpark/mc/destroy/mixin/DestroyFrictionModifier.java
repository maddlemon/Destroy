package petrolpark.mc.destroy.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;

@Mixin(IBlockStateExtension.class)
public interface DestroyFrictionModifier extends IBlockStateExtension {

    /**
     * Duplicate of {@link net.neoforged.neoforge.common.extensions.IBlockStateExtension#self self()} to avoid having to use an Accessor.
     */
    @Overwrite(remap = false)
    public default BlockState self() {
        return (BlockState)this;
    };

    /**
     * Overwritten but mostly copied from {@link net.neoforged.neoforge.common.extensions.IBlockStateExtension#getFriction Minecraft source code},
     * as Injecting into interfaces doesn't appear to be possible.
     * This decreases the friction Entities experience if they are under the {@link petrolpark.mc.destroy.content.product.alcohol.InebriationMobEffect Inebriation Effect},
     * according to the {@link petrolpark.mc.destroy.config.DestroySubstancesConfigs#drunkenSlipping config file}.
     */
    @Overwrite(remap = false)
    default float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity) {

        // Copied from the Minecraft source code
        float originalFriction = self().getBlock().getFriction(self(), level, pos, entity);
        //

        if (entity != null && entity instanceof LivingEntity livingEntity) {
            MobEffectInstance alcoholEffect = livingEntity.getEffect(DestroyMobEffects.INEBRIATION.get());
            if (alcoholEffect != null) {
                return (float)(originalFriction + ((1 - originalFriction)
                    * Math.min(5, alcoholEffect.getAmplifier() + 1) / 5 // Scale the extent of slipping with the amplifier of Inebriation (effects stop compounding after 4)
                    * (DestroyAllConfigs.SERVER.substances.drunkenSlipping.get() - 0.001f)) // Maximum level of slipping
                );
            };
        };
        
        return originalFriction;
    }
    
}
