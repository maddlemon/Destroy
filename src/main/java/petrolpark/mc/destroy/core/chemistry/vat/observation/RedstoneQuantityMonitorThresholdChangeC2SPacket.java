package petrolpark.mc.destroy.core.chemistry.vat.observation;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import petrolpark.mc.destroy.DestroyPackets;

public class RedstoneQuantityMonitorThresholdChangeC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneQuantityMonitorThresholdChangeC2SPacket> STREAM_CODEC = StreamCodec.ofMember(RedstoneQuantityMonitorThresholdChangeC2SPacket::toBytes, RedstoneQuantityMonitorThresholdChangeC2SPacket::new);

    public final float lower;
    public final float upper;
    public final BlockPos pos;

    public RedstoneQuantityMonitorThresholdChangeC2SPacket(float lower, float upper, BlockPos pos) {
        this.lower = lower;
        this.upper = upper;
        this.pos = pos;
    };

    public RedstoneQuantityMonitorThresholdChangeC2SPacket(RegistryFriendlyByteBuf buffer) {
        lower = buffer.readFloat();
        upper = buffer.readFloat();
        pos = buffer.readBlockPos();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(lower);
        buffer.writeFloat(upper);
        buffer.writeBlockPos(pos);
    };

    @Override
    public void handle(ServerPlayer sender) {
        RedstoneQuantityMonitorBehaviour behaviour = BlockEntityBehaviour.get(sender.level(), pos, RedstoneQuantityMonitorBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.lowerThreshold = lower;
            behaviour.upperThreshold = upper;
            behaviour.notifyUpdate();
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_QUANTITY_MONITOR_THRESHOLD_CHANGE;
    };
    
};
