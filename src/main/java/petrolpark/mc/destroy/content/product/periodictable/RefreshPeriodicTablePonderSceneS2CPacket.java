package petrolpark.mc.destroy.content.product.periodictable;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.client.DestroyPonderScenes;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class RefreshPeriodicTablePonderSceneS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RefreshPeriodicTablePonderSceneS2CPacket> STREAM_CODEC = StreamCodec.ofMember(RefreshPeriodicTablePonderSceneS2CPacket::toBytes, RefreshPeriodicTablePonderSceneS2CPacket::new);

    public RefreshPeriodicTablePonderSceneS2CPacket() {};

    public RefreshPeriodicTablePonderSceneS2CPacket(RegistryFriendlyByteBuf buffer) {};

    public void toBytes(RegistryFriendlyByteBuf buffer) {};

    @Override
    public void handle(LocalPlayer player) {
        DestroyPonderScenes.refreshPeriodicTableBlockScenes();
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REFRESH_PERIODIC_TABLE_PONDER_SCENE;
    };
    
};
