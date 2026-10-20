package petrolpark.mc.destroy.content.processing.trypolithography;

import petrolpark.mc.destroy.util.ItemStackNBT;
import petrolpark.mc.library.compat.create.core.world.item.transported.IDirectionalBeltItem;
import petrolpark.mc.library.util.NBTHelper;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CircuitPatternItem extends Item implements IDirectionalOnBelt {

    public CircuitPatternItem(Properties properties) {
        super(properties);
    };

    public static int getPattern(ItemStack stack) {
        if (!(stack.getItem() instanceof CircuitPatternItem || stack.getItem() instanceof SequencedAssemblyItem)) return 0;
        return NBTHelper.readBinaryMatrix4x4(ItemStackNBT.read(stack), "Pattern");
    };

    public static void putPattern(ItemStack stack, int pattern) {
        if (stack.getItem() instanceof CircuitPatternItem || stack.getItem() instanceof SequencedAssemblyItem) ItemStackNBT.update(stack, tag -> NBTHelper.writeBinaryMatrix4x4(tag, "Pattern", pattern));
    };
    
};
