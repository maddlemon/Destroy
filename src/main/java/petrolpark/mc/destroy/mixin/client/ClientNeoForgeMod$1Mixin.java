package petrolpark.mc.destroy.mixin.client;

import net.createmod.ponder.api.level.PonderLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import petrolpark.mc.destroy.core.pollution.Pollution;
import petrolpark.mc.destroy.core.pollution.SmogAffectedBlockColor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

/**
 * Smog tints Water. Targets NeoForge's anonymous Water client extensions (1.20.1 Forge: {@code ForgeMod$2$1}; NeoForge 21.1:
 * {@code ClientNeoForgeMod$1}) - re-check the index if NeoForge is updated.
 */
@Mixin(targets = "net.neoforged.neoforge.client.ClientNeoForgeMod$1")
public class ClientNeoForgeMod$1Mixin {
    
    @Overwrite(remap = false)
    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        if (getter instanceof PonderLevel ponder) return SmogAffectedBlockColor.getColor(0xFF3F76E4, ponder.getCapability(Pollution.CAPABILITY));
        return SmogAffectedBlockColor.getAverageWaterColor(getter, pos) | 0xFF000000;
    };
};
