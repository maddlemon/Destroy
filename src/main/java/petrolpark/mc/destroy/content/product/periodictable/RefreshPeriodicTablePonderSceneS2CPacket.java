package petrolpark.mc.destroy.content.product.periodictable;

import java.util.function.Supplier;

import petrolpark.mc.destroy.client.DestroyPonderScenes;
import com.petrolpark.network.packet.S2CPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent.Context;

public class RefreshPeriodicTablePonderSceneS2CPacket extends S2CPacket {

    public RefreshPeriodicTablePonderSceneS2CPacket() {};

    public RefreshPeriodicTablePonderSceneS2CPacket(FriendlyByteBuf buffer) {};

    @Override
    public void toBytes(FriendlyByteBuf buffer) {};

    @Override
    public boolean handle(Supplier<Context> supplier) {
        supplier.get().enqueueWork(DestroyPonderScenes::refreshPeriodicTableBlockScenes);
        return true;
    };
    
};
