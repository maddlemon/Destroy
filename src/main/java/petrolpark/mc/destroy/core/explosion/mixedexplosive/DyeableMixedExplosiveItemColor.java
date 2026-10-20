package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.util.FastColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

public class DyeableMixedExplosiveItemColor implements ItemColor {

    public static final DyeableMixedExplosiveItemColor INSTANCE = new DyeableMixedExplosiveItemColor();

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return -1;
        // 1.21 item tints are ARGB, so the colour must be made opaque
        return stack.getItem() instanceof DyeableMixedExplosiveBlockItem item ? FastColor.ARGB32.opaque(item.getColor(stack)) : -1;
    };
    
};
