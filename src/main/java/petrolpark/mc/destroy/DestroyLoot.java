package petrolpark.mc.destroy;

import net.neoforged.bus.api.IEventBus;

public class DestroyLoot {
    
    public static void register(IEventBus eventBus) {
        DestroyLootConditions.register(eventBus);
        DestroyNumberProviders.register(eventBus);
    };
};
