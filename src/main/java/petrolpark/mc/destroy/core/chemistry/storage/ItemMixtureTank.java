package petrolpark.mc.destroy.core.chemistry.storage;

import petrolpark.mc.destroy.util.ItemStackNBT;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour.GeniusFluidTank;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * The Fluid Handler in which {@link petrolpark.mc.destroy.core.chemistry.storage.IMixtureStorageItem IMixtureStorageItems} store Mixtures.
 */
/**
 * The Fluid Handler of Items which store Mixtures, registered in {@link petrolpark.mc.destroy.DestroyCapabilities}. A new one is made each time
 * the capability is looked up; the Fluid lives in the Item's custom data under {@code "Fluid"} (as in 1.20.1).
 */
public class ItemMixtureTank extends GeniusFluidTank implements IFluidHandlerItem {

    private final ItemStack container;

    public ItemMixtureTank(ItemStack container, Consumer<FluidStack> updateCallback) {
        super(0, updateCallback);
        if (!(container.getItem() instanceof IMixtureStorageItem item)) throw new IllegalArgumentException(container.getItem().getDescriptionId()+" cannot store Mixtures");
        capacity = item.getCapacity(container);
        this.container = container;
        fluid = ItemStackNBT.readFluid(ItemStackNBT.read(container), "Fluid");
    };

    public int getRemainingSpace() {
        return capacity - getFluidAmount();
    };

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        ItemStackNBT.update(getContainer(), customTag -> ItemStackNBT.writeFluid(customTag, "Fluid", stack));
    };

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        ItemStackNBT.update(getContainer(), customTag -> ItemStackNBT.writeFluid(customTag, "Fluid", fluid));
    };

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return DestroyFluids.isMixture(fluid);
    };

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    };
    
};
