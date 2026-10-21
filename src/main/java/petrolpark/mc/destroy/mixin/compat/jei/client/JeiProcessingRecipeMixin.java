package petrolpark.mc.destroy.mixin.compat.jei.client;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid;
import petrolpark.mc.destroy.compat.jei.DestroyJEI;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.MixtureFluidIngredient;
import petrolpark.mc.destroy.mixin.accessor.ProcessingRecipeParamsAccessor;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@Mixin(ProcessingRecipe.class)
public abstract class JeiProcessingRecipeMixin {
    
    /**
     * Injection into {@link com.simibubi.create.content.contraptions.processing.ProcessingRecipe#ProcessingRecipe ProcessingRecipe}.
     * If a Recipe produces or required a Molecule, this adds the created Recipe to the lists of Recipes in which a {@link petrolpark.mc.destroy.chemistry.legacy.LegacyMixture Mixture} is an
     * {@link petrolpark.mc.destroy.compat.jei.DestroyJEI#MOLECULES_INPUT ingredient} or {@link petrolpark.mc.destroy.compat.jei.DestroyJEI#MOLECULES_OUTPUT result}.
     */
    @SuppressWarnings("unchecked")
    @Inject(
        method = "<init>",
        at = @At(value = "RETURN"),
        remap = false
    )
    public void inInit(IRecipeTypeInfo typeInfo, ProcessingRecipeParams params, CallbackInfo ci) {
        if (!DestroyJEI.MOLECULE_RECIPES_NEED_PROCESSING) return;
        for (SizedFluidIngredient ingredient : ((ProcessingRecipeParamsAccessor)params).getFluidIngredients()) {
            if (ingredient.ingredient() instanceof MixtureFluidIngredient<?> mixtureFluidIngredient) {
                CompoundTag fluidTag = new CompoundTag();
                mixtureFluidIngredient.addNBT(fluidTag);
                for (LegacySpecies molecule : mixtureFluidIngredient.getSubType().getContainedMolecules(fluidTag)) {
                    DestroyJEI.MOLECULES_INPUT.putIfAbsent(molecule, new ArrayList<>()); // Create the List if it's not there
                    DestroyJEI.MOLECULES_INPUT.get(molecule).add((ProcessingRecipe<?, ?>)(Object)this); // Unchecked conversion (fine because this is a Mixin)
                };
            };
        };
        for (FluidStack fluidResult : ((ProcessingRecipeParamsAccessor)params).getFluidResults()) {
            if (DestroyFluids.isMixture(fluidResult)) {
                ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ReadOnlyMixture::new, MixtureFluid.getMixtureTag(fluidResult));
                for (LegacySpecies molecule : mixture.getContents(true)) {
                    DestroyJEI.MOLECULES_OUTPUT.putIfAbsent(molecule, new ArrayList<>()); // Create the List if it's not there
                    DestroyJEI.MOLECULES_OUTPUT.get(molecule).add((ProcessingRecipe<?, ?>)(Object)this); // Unchecked conversion (fine because this is a Mixin)
                };
            };
        };
    };
};
