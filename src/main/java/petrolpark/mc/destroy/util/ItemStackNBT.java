package petrolpark.mc.destroy.util;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Bridge for the 1.20.1 item NBT that Destroy kept in {@code ItemStack#getOrCreateTag()}. In 1.21 that data lives in the
 * {@link DataComponents#CUSTOM_DATA minecraft:custom_data} component (which is also where Minecraft's own data fixer puts
 * leftover 1.20 item NBT), so stored keys are unchanged. Custom data is immutable: {@link #read} hands back a copy, and
 * changes must go through {@link #update} or {@link #set}.
 * <p>TODO(port): move hot or structured keys (Blowpipe tank, Swiss Army Knife tool, Circuit Mask, ...) to typed data components.</p>
 */
public final class ItemStackNBT {

    private ItemStackNBT() {};

    /**
     * @return A copy of the Stack's custom NBT (empty if it has none). Modifying it does not change the Stack.
     */
    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    };

    /**
     * Replacement for {@code ItemStack#hasTag()}.
     */
    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    };

    /**
     * Read-modify-write the Stack's custom NBT.
     */
    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    };

    /**
     * Replace the Stack's custom NBT (an empty tag removes the component).
     */
    public static void set(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    };
};
