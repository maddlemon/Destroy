package petrolpark.mc.destroy;

import petrolpark.mc.destroy.client.DestroyItemDisplayContexts;
import petrolpark.mc.destroy.client.DestroyPartials;
import petrolpark.mc.destroy.client.DestroyParticleTypes;
import petrolpark.mc.destroy.client.DestroyPonderPlugin;
import petrolpark.mc.destroy.client.DestroySpriteSource;
import petrolpark.mc.destroy.client.FogHandler;
import petrolpark.mc.destroy.core.extendedinventory.ExtendedInventoryClientHandler;

import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class DestroyClient {
    
    public static final FogHandler FOG_HANDLER = new FogHandler();
    public static final ExtendedInventoryClientHandler EXTENDED_INVENTORY_HANDLER = new ExtendedInventoryClientHandler();

    static {
        DestroyItemDisplayContexts.register();
    };

    public static void clientInit(final FMLClientSetupEvent event) {
        // Work which must be done on main thread
        event.enqueueWork(DestroyItemProperties::register);
        //DestroyPonderTags.register();
        //DestroyPonderScenes.register();
        PonderIndex.addPlugin(new DestroyPonderPlugin());
    };

    public static void clientCtor(IEventBus modEventBus, IEventBus forgeEventBus) {
        DestroySpriteSource.register();
        DestroyPartials.init();
        modEventBus.addListener(DestroyParticleTypes::registerProviders);
    };
};
