package petrolpark.mc.destroy.core.extendedinventory;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import petrolpark.mc.destroy.DestroyPackets;

public class ExtraInventorySizeChangeS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtraInventorySizeChangeS2CPacket> STREAM_CODEC = StreamCodec.ofMember(ExtraInventorySizeChangeS2CPacket::toBytes, ExtraInventorySizeChangeS2CPacket::new);

    public final int extraInventorySize;
    public final int extraHotbarSlots;
    public final boolean requestFullState;

    public ExtraInventorySizeChangeS2CPacket(int extraInventorySize, int extraHotbarSlots, boolean requestFullState) {
        this.extraInventorySize = extraInventorySize;
        this.extraHotbarSlots = extraHotbarSlots;
        this.requestFullState = requestFullState;
    };

    public ExtraInventorySizeChangeS2CPacket(RegistryFriendlyByteBuf buffer) {
        this.extraInventorySize = buffer.readVarInt();
        this.extraHotbarSlots = buffer.readVarInt();
        this.requestFullState = buffer.readBoolean();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(extraInventorySize);
        buffer.writeVarInt(extraHotbarSlots);
        buffer.writeBoolean(requestFullState);
    };

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        ExtendedInventoryClientHandler.handleExtendedInventorySizeChange(this);
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.EXTRA_INVENTORY_SIZE_CHANGE;
    };
    
};
