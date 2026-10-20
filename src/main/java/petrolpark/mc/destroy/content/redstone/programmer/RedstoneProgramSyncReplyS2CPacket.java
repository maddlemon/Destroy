package petrolpark.mc.destroy.content.redstone.programmer;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import petrolpark.mc.destroy.DestroyPackets;

public class RedstoneProgramSyncReplyS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneProgramSyncReplyS2CPacket> STREAM_CODEC = StreamCodec.ofMember(RedstoneProgramSyncReplyS2CPacket::toBytes, RedstoneProgramSyncReplyS2CPacket::new);

    public RedstoneProgramSyncReplyS2CPacket() {

    };  
    
    public RedstoneProgramSyncReplyS2CPacket(RegistryFriendlyByteBuf buffer) {

    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {

    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.containerMenu instanceof RedstoneProgrammerMenu menu) {
            menu.refreshSlots();
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAM_SYNC_REPLY;
    };


};
