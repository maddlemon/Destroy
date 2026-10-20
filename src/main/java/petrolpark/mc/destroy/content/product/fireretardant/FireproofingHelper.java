package petrolpark.mc.destroy.content.product.fireretardant;

import java.util.Optional;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import petrolpark.mc.library.core.flags.Flag;
import petrolpark.mc.library.core.flags.Flaggables;
import petrolpark.mc.library.core.flags.ItemFlagPole;
import petrolpark.mc.library.registry.PetrolparkRegistries;

public class FireproofingHelper {

    /**
     * The library Flag (1.20.1: Contaminant) which makes Items fireproof. Defined in {@code data/destroy/petrolpark/flag/fireproof.json}.
     */
    public static final ResourceKey<Flag> FLAG_KEY = ResourceKey.create(PetrolparkRegistries.Keys.FLAG, Destroy.asResource("fireproof"));

    private static final RecipeWrapper WRAPPER = new RecipeWrapper(new ItemStackHandler());

    public static final Optional<Holder<Flag>> getFireproofFlag(RegistryAccess registryAccess) {
        return registryAccess.registry(PetrolparkRegistries.Keys.FLAG).orElseThrow().getHolder(FLAG_KEY).<Holder<Flag>>map(holder -> holder);
    };

    public static boolean canApply(Level world, ItemStack stack) {
        return couldApply(world, stack) && DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.find(WRAPPER, world).isPresent();
    };

    public static boolean couldApply(Level world, ItemStack stack) {
        if (stack.has(DataComponents.FIRE_RESISTANT) || isFireproof(world.registryAccess(), stack)) return false;
        return Flaggables.ITEM.isFlaggableStack(stack);
    };

    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (!canApply(world, stack)) return -1;
        return world.getRecipeManager().getRecipeFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType(), WRAPPER, world).stream()
            .map(SingleFluidRecipe.class::cast)
            .map(SingleFluidRecipe::getRequiredFluid)
            .filter(i -> i.test(availableFluid))
            .findFirst()
            .map(SizedFluidIngredient::amount)
            .orElse(-1);
    };

    public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        if (!canApply(world, stack)) return ItemStack.EMPTY;
        return world.getRecipeManager().getRecipeFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType(), WRAPPER, world)
            .map(SingleFluidRecipe.class::cast)
            .filter(r  -> r.getRequiredFluid().test(availableFluid))
            .map(r -> {
                availableFluid.shrink(100);
                ItemStack result = stack.copy();
                stack.shrink(1);
                apply(world, result);
                return result;
            })
            .orElse(ItemStack.EMPTY);
    };

    public static void apply(Level world, ItemStack stack) {
        getFireproofFlag(world.registryAccess()).ifPresent(ItemFlagPole.get(stack)::flag);
    };

    public static boolean isFireproof(RegistryAccess registryAccess, ItemStack stack) {
        return getFireproofFlag(registryAccess).map(ItemFlagPole.get(stack)::has).orElse(false);
    };
};
