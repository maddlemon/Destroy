package petrolpark.mc.destroy.content.product;

import java.util.function.Supplier;

import petrolpark.mc.destroy.core.item.DecayingItem;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.world.item.ItemStack;

public class CarboxylatingItem extends DecayingItem {

    // It would be cool if the lifetime was affected by the Greenhouse Gas level of the world but that is going to be a bit complicated to implement right now
    public CarboxylatingItem(Properties properties, Supplier<ItemStack> decayProduct, Supplier<ConfigBase.ConfigInt> lifetime, int defaultLifetime) {
        super(properties, decayProduct, lifetime, defaultLifetime, "item.destroy.carboxylating_item.remaining");
    };

};
