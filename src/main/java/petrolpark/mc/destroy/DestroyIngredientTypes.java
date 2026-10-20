package petrolpark.mc.destroy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitDeployerApplicationRecipe.ExampleMaskIngredient;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitPatternIngredient;

/**
 * Custom Item Ingredient types (1.20.1 registered these with {@code CraftingHelper.register} in {@code DestroyCommonModEvents}).
 */
public class DestroyIngredientTypes {

    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Destroy.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<CircuitPatternIngredient>> CIRCUIT_PATTERN_ITEM = INGREDIENT_TYPES.register("circuit_pattern_item", () -> new IngredientType<>(CircuitPatternIngredient.CODEC, CircuitPatternIngredient.STREAM_CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<ExampleMaskIngredient>> EXAMPLE_CIRCUIT_MASK = INGREDIENT_TYPES.register("example_circuit_mask", () -> new IngredientType<>(ExampleMaskIngredient.CODEC, ExampleMaskIngredient.STREAM_CODEC));

    public static final void register(IEventBus modEventBus) {
        INGREDIENT_TYPES.register(modEventBus);
    };
};
