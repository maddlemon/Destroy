package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import petrolpark.mc.destroy.DestroyPackets;

public class ChangeKeypunchPositionC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeKeypunchPositionC2SPacket> STREAM_CODEC = StreamCodec.ofMember(ChangeKeypunchPositionC2SPacket::toBytes, ChangeKeypunchPositionC2SPacket::new);

    public final BlockPos pos;
    public final int pistonPosition;

    public ChangeKeypunchPositionC2SPacket(BlockPos pos, int pistonPosition) {
        this.pos = pos;
        this.pistonPosition = pistonPosition;
    };

    public ChangeKeypunchPositionC2SPacket(RegistryFriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        pistonPosition = buffer.readVarInt();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(pistonPosition);
    };

    @Override
    public void handle(ServerPlayer sender) {
        if (sender == null) return;
        Level level = sender.level();
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KeypunchBlockEntity keypunch) {
            keypunch.setPistonPosition(pistonPosition);
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CHANGE_KEYPUNCH_POSITION;
    };
    
};
