package petrolpark.mc.destroy.mixin.compat.sodium.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import petrolpark.mc.destroy.core.pollution.SmogAffectedBlockColor;

/**
 * Sodium equivalent of the Embeddium {@code WorldSliceMixin}: Sodium's {@code LevelSlice} computes tints with its own cache, which skips
 * the Smog tint caches Destroy registers on the {@link ClientLevel} (see {@code ClientLevelMixin}). Only Destroy's Smog resolvers are
 * redirected to the Level, so every other tint keeps Sodium's fast path. Targeted by name (the class is inside Sodium's jar-in-jar);
 * only applied when Sodium is loaded (see {@code DestroyMixinPlugin}).
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice", remap = false)
public class LevelSliceMixin {

    @Shadow @Final private ClientLevel level;

    @Inject(method = "getBlockTint", at = @At("HEAD"), cancellable = true)
    public void inGetBlockTint(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
        if (resolver == SmogAffectedBlockColor.GRASS_COLOR_RESOLVER || resolver == SmogAffectedBlockColor.FOLIAGE_COLOR_RESOLVER || resolver == SmogAffectedBlockColor.WATER_COLOR_RESOLVER) {
            cir.setReturnValue(level.getBlockTint(pos, resolver));
        };
    };
};
