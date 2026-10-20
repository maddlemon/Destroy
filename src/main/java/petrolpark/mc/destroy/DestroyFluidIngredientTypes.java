package petrolpark.mc.destroy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.MixtureFluidIngredient;

/**
 * Registers every {@link petrolpark.mc.destroy.core.recipe.ingredient.fluid.MixtureFluidIngredientSubType Mixture Fluid Ingredient
 * Sub-Type} as a NeoForge {@link FluidIngredientType}, e.g. {@code destroy:mixture_fluid_with_molecule}. This replaces 1.20.1's
 * {@code FluidIngredientMixin}, which taught Create's JSON parser about the Sub-Type keys ({@code "mixtureFluidWithMolecule": ...}).
 */
public class DestroyFluidIngredientTypes {

    private static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, Destroy.MOD_ID);

    static {
        MixtureFluidIngredient.MIXTURE_FLUID_INGREDIENT_SUBTYPES.values().forEach(subType -> FLUID_INGREDIENT_TYPES.register(subType.getRegistryName(), subType::getFluidIngredientType));
    };

    public static final void register(IEventBus modEventBus) {
        FLUID_INGREDIENT_TYPES.register(modEventBus);
    };
};
