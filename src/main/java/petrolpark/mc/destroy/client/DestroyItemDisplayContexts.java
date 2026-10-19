package petrolpark.mc.destroy.client;

import petrolpark.mc.destroy.Destroy;

import net.minecraft.world.item.ItemDisplayContext;

public class DestroyItemDisplayContexts {
    
    public static final ItemDisplayContext
    
    BLOWPIPE = ItemDisplayContext.create("blowpipe", Destroy.asResource("blowpipe"), ItemDisplayContext.NONE);
    
    public static final void register() {};
};
