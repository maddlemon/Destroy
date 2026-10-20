package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import petrolpark.mc.destroy.DestroyPackets;

public class NameKeypunchC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, NameKeypunchC2SPacket> STREAM_CODEC = StreamCodec.ofMember(NameKeypunchC2SPacket::toBytes, NameKeypunchC2SPacket::new);

    public final BlockPos pos;
    public final String name;

    public NameKeypunchC2SPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    };

    public NameKeypunchC2SPacket(RegistryFriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        name = buffer.readUtf();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(name);
    };

    @Override
    public void handle(ServerPlayer sender) {
        if (sender == null) return;
        Level level = sender.level();
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KeypunchBlockEntity keypunch) {
            keypunch.name = name;
            keypunch.notifyUpdate();  
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.NAME_KEYPUNCH;
    };
    
};
