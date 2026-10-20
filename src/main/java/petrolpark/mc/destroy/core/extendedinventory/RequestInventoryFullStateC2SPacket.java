package petrolpark.mc.destroy.core.extendedinventory;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import petrolpark.mc.destroy.DestroyPackets;

public class RequestInventoryFullStateC2SPacket implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestInventoryFullStateC2SPacket> STREAM_CODEC = StreamCodec.ofMember(RequestInventoryFullStateC2SPacket::toBytes, buffer -> new RequestInventoryFullStateC2SPacket());

    public void toBytes(RegistryFriendlyByteBuf buffer) {};

    @Override
    public void handle(ServerPlayer player) {
        player.inventoryMenu.broadcastFullState();
        player.inventoryMenu.sendAllDataToRemote();
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REQUEST_INVENTORY_FULL_STATE;
    };
    
};
