package petrolpark.mc.destroy.core.pollution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.config.DestroyPollutionConfigs;
import petrolpark.mc.library.core.world.ChunkTickEvent;
import petrolpark.mc.library.util.RandomHelper;

@EventBusSubscriber
@ParametersAreNonnullByDefault
public class ChunkPollution extends Pollution<ChunkAccess> {

    public static final Serializer SERIALIZER = new ChunkPollution.Serializer();

    /**
     * Client-side only: the Smog level when this Chunk's colours were last refreshed.
     */
    private int smogLevelSinceLastRerender;

    public ChunkPollution(ChunkAccess holder, Map<PollutionType<ChunkAccess>, Integer> values) {
        super(holder, values);
        smogLevelSinceLastRerender = getPollution(DestroyPollutionTypes.SMOG.get());
    };

    /**
     * As in 1.20.1, Chunk colours are only refreshed once Smog has changed by at least 1/64 of its maximum since the last refresh.
     * @return Whether this Chunk's colours should be refreshed
     */
    public boolean checkRerender() {
        final int smog = getPollution(DestroyPollutionTypes.SMOG.get());
        if (Math.abs(smog - smogLevelSinceLastRerender) >= getProperties(DestroyPollutionTypes.SMOG.get()).max() / 64) {
            smogLevelSinceLastRerender = smog;
            return true;
        };
        return false;
    };

    public static final ChunkPollution create(IAttachmentHolder holder) {
        if (holder instanceof ChunkAccess chunk) return new ChunkPollution(chunk, DestroyRegistries.CHUNK_POLLUTION_TYPES.stream().collect(Collectors.toMap(Function.identity(), t -> 0)));
        throw new IllegalArgumentException();
    };

    @Override
    public void setChanged() {
        holder.setUnsaved(true);
    }

    @Override
    protected void syncInternal() {
        if (holder.getLevel() instanceof ServerLevel level) CatnipServices.NETWORK.sendToClientsTrackingChunk(level, holder.getPos(), new ChunkPollutionPacket(holder.getPos(), getValues()));
    };

    public void syncTo(Player entity) {
        if (entity instanceof ServerPlayer player) CatnipServices.NETWORK.sendToClient(player, new ChunkPollutionPacket(holder.getPos(), getValues()));
    };

    @Override
    public PollutionType.Properties getProperties(PollutionType<ChunkAccess> pollutionType) {
        return PollutionHelper.getChunkPollutionTypeProperties(pollutionType);
    };

    @Override
    public float getDecreaseRate(PollutionType<ChunkAccess> pollutionType) {
        return DestroyPollutionConfigs.get(DestroyAllConfigs.SERVER.pollution.pollutionDecreaseRates, DestroyRegistries.CHUNK_POLLUTION_TYPES.getKey(pollutionType), DestroyPollutionConfigs.DEFAULT_DECREASE_RATE);
    };

    public float getSpreadingRate(PollutionType<ChunkAccess> pollutionType) {
        return DestroyPollutionConfigs.get(DestroyAllConfigs.SERVER.pollution.pollutionSpreadingRates, DestroyRegistries.CHUNK_POLLUTION_TYPES.getKey(pollutionType), DestroyPollutionConfigs.DEFAULT_SPREADING_RATE);
    };

    public float getSpreadingAmount(PollutionType<ChunkAccess> pollutionType) {
        return DestroyPollutionConfigs.get(DestroyAllConfigs.SERVER.pollution.pollutionSpreadingAmounts, DestroyRegistries.CHUNK_POLLUTION_TYPES.getKey(pollutionType), DestroyPollutionConfigs.DEFAULT_SPREADING_AMOUNT);
    };

    /**
     * Decay, then spread to adjacent Chunks, as 1.20.1's {@code ServerLevelMixin#inTickChunk} did.
     */
    @Override
    public void tick(RandomSource random) {
        super.tick(random); // Decay (only if Pollution is enabled)
        final Level level = holder.getLevel();
        if (level == null || !PollutionHelper.isPollutionEnabled()) return;
        final ChunkPos pos = holder.getPos();
        if (Math.abs(pos.x) % 2 != Math.abs(pos.z) % 2) return; // Only spread to/from chunks in a checkerboard fashion, as it is really the Chunk boundaries we want to tick
        final List<ChunkPos> adjacentPositions = new ArrayList<>(List.of(new ChunkPos(pos.x - 1, pos.z), new ChunkPos(pos.x + 1, pos.z), new ChunkPos(pos.x, pos.z - 1), new ChunkPos(pos.x, pos.z + 1)));
        RandomHelper.shuffle(adjacentPositions, random);
        for (ChunkPos otherPos : adjacentPositions) {
            final ChunkAccess otherChunk = level.getChunk(otherPos.x, otherPos.z, ChunkStatus.FULL, false);
            if (otherChunk == null) continue;
            final ChunkPollution otherPollution = otherChunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION);
            for (PollutionType<ChunkAccess> pollutionType : DestroyRegistries.CHUNK_POLLUTION_TYPES) {
                if (random.nextFloat() >= getSpreadingRate(pollutionType)) continue;
                final boolean thisMorePolluted = getPollution(pollutionType) >= otherPollution.getPollution(pollutionType);
                final ChunkPollution morePolluted = thisMorePolluted ? this : otherPollution;
                final ChunkPollution lessPolluted = thisMorePolluted ? otherPollution : this;
                final int transfer = (int)(0.5f + (morePolluted.getPollution(pollutionType) - lessPolluted.getPollution(pollutionType)) * 0.5f * getSpreadingAmount(pollutionType));
                if (transfer == 0) continue;
                morePolluted.changePollution(pollutionType, -transfer);
                lessPolluted.changePollution(pollutionType, transfer);
            };
        };
    };

    public static class Serializer extends Pollution.Serializer<ChunkAccess, ChunkPollution> {

        public Serializer() {
            super(DestroyRegistries.CHUNK_POLLUTION_TYPES);
        };

        @Override
        protected ChunkPollution create(ChunkAccess holder, Map<PollutionType<ChunkAccess>, Integer> values) {
            return new ChunkPollution(holder, values);
        };

        @Override
        public ChunkAccess castHolder(IAttachmentHolder holder) {
            if (holder instanceof ChunkAccess chunk) return chunk;
            throw new IllegalArgumentException();
        };

    };

    @SubscribeEvent
    public static final void onWatchChunk(ChunkWatchEvent.Sent event) {
        event.getChunk().getData(DestroyAttachmentTypes.CHUNK_POLLUTION).syncTo(event.getPlayer());
    };

    /**
     * Fired at the end of {@code ServerLevel.tickChunk}, where 1.20.1 injected.
     */
    @SubscribeEvent
    public static final void onTickChunk(ChunkTickEvent.Post event) {
        event.getChunk().getData(DestroyAttachmentTypes.CHUNK_POLLUTION).tick(event.getChunk().getLevel().getRandom());
    };
    
};
