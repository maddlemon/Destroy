package petrolpark.mc.destroy.util;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
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

    /**
     * Read a Fluid Stack stored in Item NBT by {@link #writeFluid}. Items have no registry access, so this uses plain NBT; that is enough for
     * Destroy's Fluids (the Fluid ID, the Mixture component and potion contents all serialize without registries).
     * @return The Fluid Stack, or empty if there is none or it cannot be read
     */
    public static FluidStack readFluid(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) return FluidStack.EMPTY;
        return FluidStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, tag.getCompound(key)).result().orElse(FluidStack.EMPTY);
    };

    /**
     * Store a Fluid Stack in Item NBT (replaces 1.20.1's {@code FluidStack#writeToNBT}).
     */
    public static void writeFluid(CompoundTag tag, String key, FluidStack fluidStack) {
        tag.put(key, FluidStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, fluidStack).result().orElseGet(CompoundTag::new));
    };
};
