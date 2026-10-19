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
import com.petrolpark.recipe.advancedprocessing.AdvancedProcessingRecipeSerializer;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public enum DestroyRecipeTypes implements IRecipeTypeInfo {
    // Processing recipes
    AGING(AgeingRecipe::new),
    ARC_FURNACE(ArcFurnaceRecipe::new),
    CENTRIFUGATION(CentrifugationRecipe::new),
    CHARGING(ChargingRecipe::new),
    CIRCUIT_DEPLOYING(CircuitDeployerApplicationRecipe::new, AllRecipeTypes.DEPLOYING::getType),
    DISC_ELECTROPLATING(DiscElectroplatingRecipe::new),
    DISTILLATION(DistillationRecipe::new),
    ELECTROLYSIS(ElectrolysisRecipe::new),
    ELEMENT_TANK_FILLING(ElementTankFillingRecipe::new),
    EXTRUSION(ExtrusionRecipe::new),
    FLAME_RETARDANT_APPLICATION(FlameRetardantApplicationRecipe::new),
    GLASSBLOWING(GlassblowingRecipe::new),
    MIXTURE_CONVERSION(MixtureConversionRecipe::new),
    MUTATION(PhytominingRecipe::new),
    OBLITERATION(ObliterationRecipe::new),
    REACTION(ReactionRecipe::new),
    SIEVING(SievingRecipe::new),
    TAPPING(TappingRecipe::new),
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

    DestroyRecipeTypes(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory) {
        this(() -> new AdvancedProcessingRecipeSerializer<>(processingFactory));
    };

    DestroyRecipeTypes(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory, Supplier<RecipeType<?>> typeSupplier) {
        this(() -> new AdvancedProcessingRecipeSerializer<>(processingFactory), typeSupplier);
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
    public <T extends RecipeType<?>> T getType() {
        return (T) type.get();
    };

    public boolean is(Recipe<?> recipe) {
        return recipe.getType() == this.getType();
    };

    public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
        return world.getRecipeManager()
            .getRecipeFor(getType(), inv, world);
    };

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(NeoForgeRegistries.RECIPE_SERIALIZERS, Destroy.MOD_ID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Destroy.MOD_ID);
    };
    
}
