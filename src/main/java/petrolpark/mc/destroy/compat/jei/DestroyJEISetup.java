package petrolpark.mc.destroy.compat.jei;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import petrolpark.mc.library.compat.jei.PetrolparkJEISetup;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyItems;

import net.minecraft.world.item.ItemStack;

/**
 * This class will be loaded without the guarantee that JEI is installed.
 */
public class DestroyJEISetup {
    
    /**
     * Any Item which can be filled with Explosives.
     */
    public static final Collection<Supplier<ItemStack>> CUSTOM_MIX_EXPLOSIVES = new HashSet<>();
    static {
        CUSTOM_MIX_EXPLOSIVES.add(DestroyBlocks.CUSTOM_EXPLOSIVE_MIX::asStack);
    };

    static {
        PetrolparkJEISetup.DECAYING_ITEMS.add(DestroyItems.QUICKLIME::asStack);
        PetrolparkJEISetup.DECAYING_ITEMS.add(DestroyItems.SODIUM_INGOT::asStack);
    };
};
