package petrolpark.mc.destroy.content.processing.trypolithography;

import petrolpark.mc.destroy.util.ItemStackNBT;
import petrolpark.mc.library.compat.create.core.world.item.transported.DirectionalTransportedItemStack;
import petrolpark.mc.library.compat.create.core.world.item.transported.IDirectionalBeltItem;
import petrolpark.mc.library.util.BinaryMatrix4x4;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CircuitPatternItem extends Item implements IDirectionalBeltItem<DirectionalTransportedItemStack> {

    public CircuitPatternItem(Properties properties) {
        super(properties);
    };

    @Override
    public DirectionalTransportedItemStack makeTransportedItemStack(TransportedItemStack transportedItemStack) {
        return DirectionalTransportedItemStack.copyFully(transportedItemStack); // 1.20.1 IDirectionalOnBelt's default
    };

    public static int getPattern(ItemStack stack) {
        if (!(stack.getItem() instanceof CircuitPatternItem || stack.getItem() instanceof SequencedAssemblyItem)) return 0;
        // Same storage as 1.20.1's NBTHelper.readBinaryMatrix4x4: a short
        CompoundTag tag = ItemStackNBT.read(stack);
        if (!tag.contains("Pattern", Tag.TAG_SHORT)) return 0;
        return BinaryMatrix4x4.fromShort(tag.getShort("Pattern"));
    };

    public static void putPattern(ItemStack stack, int pattern) {
        if (stack.getItem() instanceof CircuitPatternItem || stack.getItem() instanceof SequencedAssemblyItem) ItemStackNBT.update(stack, tag -> tag.putShort("Pattern", BinaryMatrix4x4.asShort(pattern)));
    };
    
};
