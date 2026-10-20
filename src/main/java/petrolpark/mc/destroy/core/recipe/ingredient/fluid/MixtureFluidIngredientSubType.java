package petrolpark.mc.destroy.core.recipe.ingredient.fluid;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

public abstract class MixtureFluidIngredientSubType<T extends MixtureFluidIngredient<T>> {

    private FluidIngredientType<T> fluidIngredientType;

    public abstract T getNew();

    /**
     * The NeoForge type this Sub-Type is registered as (see {@link petrolpark.mc.destroy.DestroyFluidIngredientTypes}).
     */
    public final FluidIngredientType<T> getFluidIngredientType() {
        if (fluidIngredientType == null) fluidIngredientType = new FluidIngredientType<>(codec(), streamCodec());
        return fluidIngredientType;
    };

    /**
     * Registry path of this Sub-Type: the 1.20.1 JSON key in snake case (e.g. {@code mixtureFluidWithMolecule} -> {@code mixture_fluid_with_molecule}).
     */
    public final String getRegistryName() {
        return getMixtureFluidIngredientSubtype().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    };

    /**
     * Codec bridging to the Sub-Type's existing JSON reading and writing ({@link MixtureFluidIngredient#readInternal(JsonObject)}),
     * so its fields and validation are exactly as in 1.20.1.
     */
    protected MapCodec<T> codec() {
        return new MapCodec<T>() {

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return Stream.empty();
            };

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                JsonObject json = new JsonObject();
                input.entries().forEach(entry -> ops.getStringValue(entry.getFirst()).result().ifPresent(key -> json.add(key, ops.convertTo(JsonOps.INSTANCE, entry.getSecond()))));
                json.remove("type");
                try {
                    T ingredient = getNew();
                    ingredient.readInternal(json);
                    return DataResult.success(ingredient);
                } catch (RuntimeException e) {
                    return DataResult.error(() -> "Invalid " + getRegistryName() + " fluid ingredient " + json + ": " + e.getMessage());
                }
            };

            @Override
            public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                for (java.util.Map.Entry<String, JsonElement> entry : input.toJson().entrySet()) {
                    prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
                };
                return prefix;
            };
        };
    };

    protected StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return StreamCodec.of((buffer, ingredient) -> ingredient.writeInternal(buffer), buffer -> {
            T ingredient = getNew();
            ingredient.readInternal(buffer);
            return ingredient;
        });
    };

    /**
     * Get the string which is used to identify this ingredient type in the JSON recipe deserializer.
     * @return
     */
    public abstract String getMixtureFluidIngredientSubtype();

    /**
     * Generate a tooltip to act as the description of an ingredient in JEI. All data should come from {@code
     * fluidTag}. You should add this data {@link MixtureFluidIngredient#addNBT here}.
     * @param fluidTag
     */
    public abstract List<Component> getDescription(CompoundTag fluidTag);

    /**
     * Get the Molecules which this ingredient could contain, so if this Ingredient is clicked in JEI, it knows
     * which Molecules to look up. All data should come from {@code fluidTag}. You should add this data {@link
     * MixtureFluidIngredient#addNBT here}.
     * @param fluidTag
     */
    public abstract Collection<LegacySpecies> getContainedMolecules(CompoundTag fluidTag);
};
