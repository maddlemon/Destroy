package petrolpark.mc.destroy;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.content.processing.ageing.AgeingRecipe;
import petrolpark.mc.destroy.content.processing.centrifuge.CentrifugationRecipe;
import petrolpark.mc.destroy.content.processing.discstamping.DiscElectroplatingRecipe;
import petrolpark.mc.destroy.content.processing.distillation.DistillationRecipe;
import petrolpark.mc.destroy.content.processing.dynamo.ChargingRecipe;
import petrolpark.mc.destroy.content.processing.dynamo.ElectrolysisRecipe;
import petrolpark.mc.destroy.content.processing.dynamo.arcfurnace.ArcFurnaceRecipe;
import petrolpark.mc.destroy.content.processing.extrusion.ExtrusionRecipe;
import petrolpark.mc.destroy.content.processing.glassblowing.GlassblowingRecipe;
import petrolpark.mc.destroy.content.processing.phytomining.PhytominingRecipe;
import petrolpark.mc.destroy.content.processing.sieve.SievingRecipe;
import petrolpark.mc.destroy.content.processing.treetap.TappingRecipe;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitDeployerApplicationRecipe;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitSequencedAssemblyRecipe;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.ManualCircuitBoardRecipe;
import petrolpark.mc.destroy.content.product.fireretardant.FlameRetardantApplicationRecipe;
import petrolpark.mc.destroy.content.product.periodictable.ElementTankFillingRecipe;
import petrolpark.mc.destroy.core.chemistry.recipe.MixtureConversionRecipe;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe;
import petrolpark.mc.destroy.core.explosion.ExtendedDurationFireworkRocketRecipe;
import petrolpark.mc.destroy.core.explosion.ObliterationRecipe;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.FillMixedExplosiveItemRecipe;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Modelled on Create's {@code AllRecipeTypes}. Processing recipes use the Petrolpark Library's
 * {@link AdvancedProcessingRecipe.Serializer} (adds {@code biomes}, {@code book_required} and {@code first_time_lucky_key} to Create's
 * processing recipe format) like 1.20.1's {@code AdvancedProcessingRecipeSerializer}; Basin recipes use Create's own serializer.
 */
public enum DestroyRecipeTypes implements IRecipeTypeInfo {
    // Processing recipes
    AGING(advanced(AgeingRecipe::new)),
    ARC_FURNACE(standard(ArcFurnaceRecipe::new)),
    CENTRIFUGATION(advanced(CentrifugationRecipe::new)),
    CHARGING(advanced(ChargingRecipe::new)),
    CIRCUIT_DEPLOYING(itemApplication(CircuitDeployerApplicationRecipe::new), AllRecipeTypes.DEPLOYING::getType),
    DISC_ELECTROPLATING(standard(DiscElectroplatingRecipe::new)),
    DISTILLATION(advanced(DistillationRecipe::new)),
    ELECTROLYSIS(standard(ElectrolysisRecipe::new)),
    ELEMENT_TANK_FILLING(advanced(ElementTankFillingRecipe::new)),
    EXTRUSION(advanced(ExtrusionRecipe::new)),
    FLAME_RETARDANT_APPLICATION(advanced(FlameRetardantApplicationRecipe::new)),
    GLASSBLOWING(GlassblowingRecipe.Serializer::new),
    MIXTURE_CONVERSION(advanced(MixtureConversionRecipe::new)),
    MUTATION(advanced(PhytominingRecipe::new)),
    OBLITERATION(advanced(ObliterationRecipe::new)),
    REACTION(advanced(ReactionRecipe::new)),
    SIEVING(advanced(SievingRecipe::new)),
    TAPPING(advanced(TappingRecipe::new)),
    CIRCUIT_SEQUENCED_ASSEMBLY(CircuitSequencedAssemblyRecipe.Serializer::new, AllRecipeTypes.SEQUENCED_ASSEMBLY::getType),

    // Advanced Crafting Table recipes
    CIRCUIT_BOARD_MANUAL_CRAFTING(ManualCircuitBoardRecipe.Serializer::new, () -> RecipeType.CRAFTING),
    DURATION_4_FIREWORK_ROCKET_CRAFTING(() -> ExtendedDurationFireworkRocketRecipe.DURATION_4_FIREWORK_ROCKET, () -> RecipeType.CRAFTING),
    DURATION_5_FIREWORK_ROCKET_CRAFTING(() -> ExtendedDurationFireworkRocketRecipe.DURATION_5_FIREWORK_ROCKET, () -> RecipeType.CRAFTING),
    FILL_CUSTOM_EXPLOSIVE_MIX_ITEM(() -> FillMixedExplosiveItemRecipe.SERIALIZER, () -> RecipeType.CRAFTING);

    // This is alllllll copied from Create source code
    private final ResourceLocation id;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
    @Nullable
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    DestroyRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier) {
        this(serializerSupplier, typeSupplier, false);
    };

    DestroyRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
        String name = Lang.asId(name());
        id = Destroy.asResource(name);
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        if (registerType) {
            typeObject = Registers.TYPE_REGISTER.register(name, typeSupplier);
            type = typeObject;
        } else {
            typeObject = null;
            type = typeSupplier;
        };
    };

    DestroyRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        id = Destroy.asResource(name);
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
        type = typeObject;
    };

    /**
     * Serializer for a recipe extending the Petrolpark Library's {@link AdvancedProcessingRecipe}.
     */
    private static Supplier<RecipeSerializer<?>> advanced(ProcessingRecipe.Factory<AdvancedProcessingRecipeParams, ? extends AdvancedProcessingRecipe<?>> factory) {
        return () -> new AdvancedProcessingRecipe.Serializer<>(factory);
    };

    /**
     * Serializer for a recipe extending Create's {@link StandardProcessingRecipe} (e.g. Basin recipes).
     */
    private static Supplier<RecipeSerializer<?>> standard(StandardProcessingRecipe.Factory<?> factory) {
        return () -> new StandardProcessingRecipe.Serializer<>(factory);
    };

    /**
     * Serializer for a recipe extending Create's {@link ItemApplicationRecipe} (e.g. Deployer recipes).
     */
    private static Supplier<RecipeSerializer<?>> itemApplication(ProcessingRecipe.Factory<ItemApplicationRecipeParams, ? extends ItemApplicationRecipe> factory) {
        return () -> new ItemApplicationRecipe.Serializer<>(factory);
    };

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZER_REGISTER.register(modEventBus);
        Registers.TYPE_REGISTER.register(modEventBus);
    };

    @Override
    public ResourceLocation getId() {
        return id;
    };

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject.get();
    };

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    };

    public boolean is(Recipe<?> recipe) {
        return recipe.getType() == this.getType();
    };

    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I inv, Level world) {
        return world.getRecipeManager().getRecipeFor(this.<I, R>getType(), inv, world);
    };

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Destroy.MOD_ID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Destroy.MOD_ID);
    };

}
