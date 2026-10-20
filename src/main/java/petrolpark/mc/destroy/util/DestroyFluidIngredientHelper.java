package petrolpark.mc.destroy.util;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class DestroyFluidIngredientHelper {

    private DestroyFluidIngredientHelper() {};

    /**
     * Replacement for 1.20.1 Create's {@code FluidIngredient.fromFluidStack}: matches the Fluid and (non-strictly) its data components,
     * like the old ingredient matched the Fluid and its NBT. {@code SizedFluidIngredient.of(FluidStack)} would ignore the components,
     * so e.g. any potion would match a specific potion ingredient.
     */
    public static SizedFluidIngredient fromFluidStack(FluidStack stack) {
        return new SizedFluidIngredient(DataComponentFluidIngredient.of(false, stack), stack.getAmount());
    };
};
