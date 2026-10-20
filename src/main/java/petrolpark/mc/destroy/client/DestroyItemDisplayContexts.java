package petrolpark.mc.destroy.client;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Display contexts added to {@link ItemDisplayContext} through {@code META-INF/enumextensions.json} (serialized name {@code destroy:blowpipe}, falling back to {@code NONE}, as in 1.20.1).
 */
public class DestroyItemDisplayContexts {

    public static final ItemDisplayContext

    BLOWPIPE = ItemDisplayContext.valueOf("DESTROY_BLOWPIPE");

    public static final void register() {};
};
