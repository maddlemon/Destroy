package petrolpark.mc.destroy.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@Mixin(ProcessingRecipeParams.class)
public interface ProcessingRecipeParamsAccessor {

    @Accessor(
        value = "fluidIngredients",
        remap = false
    )
    public NonNullList<SizedFluidIngredient> getFluidIngredients();

    @Accessor(
        value = "fluidResults",
        remap = false
    )
	public NonNullList<FluidStack> getFluidResults();
};
