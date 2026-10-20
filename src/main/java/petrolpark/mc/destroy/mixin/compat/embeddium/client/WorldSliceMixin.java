package petrolpark.mc.destroy.mixin.compat.embeddium.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;

/**
 * Embeddium's chunk-building {@code WorldSlice} computes tints with its own cache, which skips the Smog tint caches Destroy registers on
 * the {@link ClientLevel} (see {@code ClientLevelMixin}). As in 1.20.1, send every tint lookup to the Level instead.
 * Targeted by name so Embeddium isn't needed to compile; only applied when Embeddium is loaded (see {@code DestroyMixinPlugin}).
 */
@Mixin(targets = "org.embeddedt.embeddium.impl.world.WorldSlice", remap = false)
public class WorldSliceMixin {

    @Shadow @Final public ClientLevel world;

    /**
     * @author Destroy
     * @reason Use the Level's tint caches so Smog colours Grass, Foliage and Water (same as the 1.20.1 Embeddium compat)
     */
    @Overwrite
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return this.world.getBlockTint(pos, resolver);
    };
};
