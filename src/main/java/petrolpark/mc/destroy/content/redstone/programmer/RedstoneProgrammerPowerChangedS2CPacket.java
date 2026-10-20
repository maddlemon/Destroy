package petrolpark.mc.destroy.content.redstone.programmer;

import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerMenu.DummyRedstoneProgram;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class RedstoneProgrammerPowerChangedS2CPacket implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, RedstoneProgrammerPowerChangedS2CPacket> STREAM_CODEC = StreamCodec.ofMember(RedstoneProgrammerPowerChangedS2CPacket::toBytes, RedstoneProgrammerPowerChangedS2CPacket::new);

    public final boolean powered;

    public RedstoneProgrammerPowerChangedS2CPacket(boolean powered) {
        this.powered = powered;
    };

    public RedstoneProgrammerPowerChangedS2CPacket(RegistryFriendlyByteBuf buffer) {
        powered = buffer.readBoolean();
    };

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(powered);
    };

    @Override
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RedstoneProgrammerScreen screen) {
            if (screen.program instanceof DummyRedstoneProgram program) program.powered = powered;
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.REDSTONE_PROGRAMMER_POWER_CHANGED;
    };
    
};
