package petrolpark.mc.destroy.core.recipe.ingredient.fluid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.DestroyFluidIngredientTypes;
import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.chemistry.legacy.LegacyMixture;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

/**
 * A Fluid Ingredient which matches Mixtures by their contents.
 * <p>1.21: these are NeoForge {@link FluidIngredient}s, each Sub-Type registered as a {@link FluidIngredientType} in
 * {@link DestroyFluidIngredientTypes} (JSON {@code "type": "destroy:<id>"}). The amount is no longer part of the ingredient -
 * it's the {@link net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient} wrapping it. Each Sub-Type still parses and writes
 * its own fields with {@link #readInternal(JsonObject)} / {@link #writeInternal(JsonObject)} (bridged to codecs in
 * {@link MixtureFluidIngredientSubType}), so the 1.20.1 parsing rules are unchanged.</p>
 */
public abstract class MixtureFluidIngredient<T extends MixtureFluidIngredient<T>> extends FluidIngredient {

    public static final Map<String, MixtureFluidIngredientSubType<?>> MIXTURE_FLUID_INGREDIENT_SUBTYPES = new HashMap<>();

    static {
        registerMixtureFluidIngredientSubType(MoleculeFluidIngredient.TYPE);
        registerMixtureFluidIngredientSubType(SaltFluidIngredient.TYPE);
        registerMixtureFluidIngredientSubType(MoleculeTagFluidIngredient.TYPE);
        registerMixtureFluidIngredientSubType(RefrigerantDummyFluidIngredient.TYPE);
        registerMixtureFluidIngredientSubType(IonFluidIngredient.TYPE);
        registerMixtureFluidIngredientSubType(PureSpeciesFluidIngredient.TYPE);
    };

    public static void registerMixtureFluidIngredientSubType(MixtureFluidIngredientSubType<?> ingredientType) {
        MIXTURE_FLUID_INGREDIENT_SUBTYPES.put(ingredientType.getMixtureFluidIngredientSubtype(), ingredientType);
    };

    @Override
    public final boolean test(FluidStack fluidStack) {
        if (!(fluidStack.getFluid().getFluidType() == DestroyFluids.MIXTURE.getType())) return false; // If it's not a Mixture
        CompoundTag mixtureTag = MixtureFluid.getMixtureTag(fluidStack);
        if (mixtureTag.isEmpty()) return false; // If this Mixture Fluid has no associated Mixture
        return testMixture(LegacyMixture.readNBT(mixtureTag));
    };

    /**
     * Example Fluid Stacks (for recipe viewers). Each carries {@link DestroyDataComponentTypes#MIXTURE_INGREDIENT_DISPLAY} - the same
     * data 1.20.1 put in the Fluid Stack's NBT - so the ingredient's description can be shown.
     */
    @Override
    protected final Stream<FluidStack> generateStacks() {
        CompoundTag displayTag = new CompoundTag();
        displayTag.putString("MixtureFluidIngredientSubtype", getSubType().getMixtureFluidIngredientSubtype());
        addNBT(displayTag);
        return getExampleMixtures().stream().map(mixture -> {
            FluidStack stack = MixtureFluid.of(1000, mixture); // The amount is set by the SizedFluidIngredient
            stack.set(DestroyDataComponentTypes.MIXTURE_INGREDIENT_DISPLAY, displayTag.copy());
            return stack;
        });
    };

    @Override
    public boolean isSimple() {
        return false;
    };

    @Override
    public final FluidIngredientType<?> getType() {
        return getSubType().getFluidIngredientType();
    };

    public abstract MixtureFluidIngredientSubType<T> getSubType();

    protected abstract boolean testMixture(LegacyMixture mixture);

    /**
     * Add data to the NBT of the Fluid Ingredient when it is displayed in JEI. The only use of this is to control the
     * {@link MixtureFluidIngredientSubType#getDescription description}. Careful not to overwite the tag
     * {@code MixtureFluidIngredientSubtype}.
     * @param fluidTag
     */
    public abstract void addNBT(CompoundTag fluidTag);

    protected abstract void readInternal(FriendlyByteBuf buffer);

    protected abstract void writeInternal(FriendlyByteBuf buffer);

    /**
     * Read this ingredient's fields (everything except {@code type} and {@code amount}).
     * @throws RuntimeException if the JSON is invalid; the message is reported as the codec error
     */
    protected abstract void readInternal(JsonObject json);

    protected abstract void writeInternal(JsonObject json);

    /**
     * Get the Mixtures this ingredient should cycle through when shown in JEI.
     * @return
     */
    public List<ReadOnlyMixture> getExampleMixtures() {
        return List.of();
    };

    protected JsonObject toJson() {
        JsonObject json = new JsonObject();
        writeInternal(json);
        return json;
    };

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MixtureFluidIngredient<?> other && other.getSubType() == getSubType() && other.toJson().equals(toJson());
    };

    @Override
    public int hashCode() {
        return 31 * getSubType().hashCode() + toJson().hashCode();
    };

};
