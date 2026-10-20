package petrolpark.mc.destroy.core.pollution;

import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.core.pollution.PollutionType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientLevelPollutionData {
    
    private static Pollution levelPollution;

    private static Integer lastRenderedSmogLevel = null;

    public static void setLevelPollution(Pollution levelPollution) {
        ClientLevelPollutionData.levelPollution = levelPollution;
        if (lastRenderedSmogLevel == null || Math.abs(lastRenderedSmogLevel - levelPollution.get(DestroyPollutionTypes.SMOG.get())) >= 1000) {
            Minecraft mc = Minecraft.getInstance();
            for (RenderChunk chunk : mc.levelRenderer.viewArea.chunks) chunk.setDirty(false); // Re-render all chunks
            lastRenderedSmogLevel = levelPollution.get(DestroyPollutionTypes.SMOG.get());
        };
    };

    public static Pollution getLevelPollution() {
        return levelPollution;
    };
}
