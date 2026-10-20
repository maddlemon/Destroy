package petrolpark.mc.destroy.core.player;

import petrolpark.mc.destroy.DestroyAttachmentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import java.util.Optional;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.EvictingQueue;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class PlayerPreviousPositionsCapability implements INBTSerializable<CompoundTag> {

    /**
     * Players only (as the old capability was).
     */
    public static Optional<PlayerPreviousPositionsCapability> of(Entity entity) {
        return entity instanceof Player ? Optional.of(entity.getData(DestroyAttachmentTypes.PLAYER_PREVIOUS_POSITIONS)) : Optional.empty();
    };

    /**
     * Attachment factory: reads the queue size from the config before creating the queue (as the old Provider did).
     */
    public static PlayerPreviousPositionsCapability create() {
        updateQueueSize();
        return new PlayerPreviousPositionsCapability();
    };

    private static int QUEUE_SIZE = 20; // Default is 20
    private static final int TICKS_PER_SECOND = 20;

    private Queue<BlockPos> previousPositions = EvictingQueue.create(QUEUE_SIZE);
    private int tickCounter = 0;

    public BlockPos getOldestPosition() {
        return previousPositions.peek();
    };

    public void recordPosition(BlockPos pos) {
        previousPositions.add(pos);
    };

    public void clearPositions() {
        previousPositions.clear();
    };

    public void incrementTickCounter() {
        tickCounter++;
        if (tickCounter >= TICKS_PER_SECOND) {
            tickCounter = 0;
        };
    };

    public boolean hasBeenSecond() {
        return tickCounter == 0;
    };

    public void saveNBTData(CompoundTag tag) {
        ListTag positionsTag = new ListTag();
        for (BlockPos pos : previousPositions) {
            positionsTag.add(new IntArrayTag(List.of(pos.getX(), pos.getY(), pos.getZ())));
        };
        tag.put("PreviousPositions", positionsTag);
    };

    public void loadNBTData(CompoundTag tag) {
        previousPositions = EvictingQueue.create(QUEUE_SIZE);
        ListTag positionsTag = tag.getList("PreviousPositions", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < positionsTag.size(); i++) {
            int[] posTag = positionsTag.getIntArray(i);
            previousPositions.add(new BlockPos(posTag[0], posTag[1], posTag[2]));
        };
    };

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveNBTData(tag);
        return tag;
    };

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        loadNBTData(tag);
    };

    public static void updateQueueSize() {
        QUEUE_SIZE = DestroyAllConfigs.SERVER.substances.chorusWineTeleportTime.get();
    };

    public static int getQueueSize() {
        return QUEUE_SIZE;
    };

    @SubscribeEvent
    public static final void onPlayerTickPre(PlayerTickEvent.Pre event) {
        onPlayerTick(event.getEntity());
    };

    // Forge fired this tick event at both START and END and the handler ignored the phase, so it ran twice per tick. Kept that way to match 1.20.1.
    @SubscribeEvent
    public static final void onPlayerTickPost(PlayerTickEvent.Post event) {
        onPlayerTick(event.getEntity());
    };

    public static final void onPlayerTick(Player player) {
        if (!player.level().isClientSide()) PlayerPreviousPositionsCapability.of(player).ifPresent((playerPreviousPositions -> {
            playerPreviousPositions.incrementTickCounter();
            if (playerPreviousPositions.hasBeenSecond()) playerPreviousPositions.recordPosition(player.blockPosition());
        }));
    };
    
};
