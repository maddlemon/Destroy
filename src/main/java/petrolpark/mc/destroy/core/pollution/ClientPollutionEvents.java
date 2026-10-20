package petrolpark.mc.destroy.core.pollution;

import net.minecraft.world.level.ChunkPos;
import petrolpark.mc.library.util.ColorHelper;

/**
 * Smog's colouring of grass, foliage and water is done as in 1.20.1, by {@link SmogAffectedBlockColor} and the tint caches
 * {@code ClientLevelMixin} adds; smog fog is done by {@link petrolpark.mc.destroy.client.FogHandler}.
 */
public class ClientPollutionEvents {

    /**
     * Re-render the Chunks around this one with up-to-date Smog colours.
     */
    public static final void refreshSmog(ChunkPos pos) {
        ColorHelper.refreshChunkColors(pos);
    };
};
