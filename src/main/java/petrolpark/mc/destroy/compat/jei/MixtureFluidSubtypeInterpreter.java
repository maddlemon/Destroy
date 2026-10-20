package petrolpark.mc.destroy.compat.jei;

import petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid;
import petrolpark.mc.destroy.compat.jei.recipemanager.ChemicalSpeciesRecipeManagerPlugin;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * The proper interpretation of Mixtures is done by interpreting all of the contents of the Mixture separately in {@link ChemicalSpeciesRecipeManagerPlugin}.
*/ 
public class MixtureFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {

    @Override
    public String apply(FluidStack ingredient, UidContext context) {
        CompoundTag mixtureTag = MixtureFluid.getMixtureTag(ingredient);
        if (mixtureTag.isEmpty()) return IIngredientSubtypeInterpreter.NONE;
        return mixtureTag.toString();
    };
    
};
