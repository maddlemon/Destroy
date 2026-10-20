package petrolpark.mc.destroy.core.seismology;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import petrolpark.mc.destroy.DestroyPackets;

public class SeismometerSpikePacket implements ClientboundPacketPayload {

    public static final SeismometerSpikePacket INSTANCE = new SeismometerSpikePacket();

    private SeismometerSpikePacket() {};

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        SeismometerItemRenderer.spike();
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SEISMOMETER_SPIKE;
    };
    
};
