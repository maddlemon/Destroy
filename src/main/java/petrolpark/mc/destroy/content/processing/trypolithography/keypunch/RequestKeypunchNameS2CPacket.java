package petrolpark.mc.destroy.content.processing.trypolithography.keypunch;

import net.createmod.catnip.platform.CatnipServices;
import java.util.List;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.util.NameLists;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class RequestKeypunchNameS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestKeypunchNameS2CPacket> STREAM_CODEC = StreamCodec.ofMember(RequestKeypunchNameS2CPacket::toBytes, RequestKeypunchNameS2CPacket::new);

    public final BlockPos pos;

    public RequestKeypunchNameS2CPacket(BlockPos pos) {
        this.pos = pos;
    };

    public RequestKeypunchNameS2CPacket(RegistryFriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    };

    @Override
    public void handle(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        List<String> names = NameLists.getNames(KeypunchBlock.NAME_LIST_ID);
        CatnipServices.NETWORK.sendToServer(new NameKeypunchC2SPacket(pos, names.get(minecraft.level.getRandom().nextInt(names.size()))));
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REQUEST_KEYPUNCH_NAME;
    };
    
};
