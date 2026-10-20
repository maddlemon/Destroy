package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import petrolpark.mc.destroy.util.ItemStackNBT;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyIngredientTypes;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;
import petrolpark.mc.destroy.mixin.accessor.ProcessingRecipeAccessor;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * Uses Create's Item Application serializer: 1.20.1 had a {@code Serializer} here that validated the deployed Item, but it was never registered
 * ({@code CIRCUIT_DEPLOYING} used the library's generic serializer), so it is not ported.
 */
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class CircuitDeployerApplicationRecipe extends DeployerApplicationRecipe implements IConfersCircuitPatternRecipe {

    private final boolean example;

    public static int EXAMPLE_PATTERN = 0b1000001000010100;

    private int recipeId = 0;

    /**
     * Shows the parent Ingredient's Items with {@link CircuitDeployerApplicationRecipe#EXAMPLE_PATTERN}. Should never actually appear in a JSON recipe
     * (JSON: {@code {"type": "destroy:example_circuit_mask", "ingredient": ...}}).
     */
    public static class ExampleMaskIngredient implements ICustomIngredient {

        public static final MapCodec<ExampleMaskIngredient> CODEC = Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").xmap(ExampleMaskIngredient::new, i -> i.parent);
        public static final StreamCodec<RegistryFriendlyByteBuf, ExampleMaskIngredient> STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.map(ExampleMaskIngredient::new, i -> i.parent);

        private final Ingredient parent;

        public ExampleMaskIngredient(Ingredient parent) {
            this.parent = parent;
        };

        @Override
        public boolean test(ItemStack stack) {
            return parent.test(stack);
        };

        @Override
        public Stream<ItemStack> getItems() {
            return Stream.of(parent.getItems()).map(stack -> {
                stack = stack.copy();
                ItemStackNBT.update(stack, customTag -> customTag.putBoolean("HideContaminants", true));
                CircuitPatternItem.putPattern(stack, EXAMPLE_PATTERN);
                return stack;
            });
        };

        @Override
        public boolean isSimple() {
            return false;
        };

        @Override
        public IngredientType<?> getType() {
            return DestroyIngredientTypes.EXAMPLE_CIRCUIT_MASK.get();
        };

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ExampleMaskIngredient other && other.parent.equals(parent);
        };

        @Override
        public int hashCode() {
            return parent.hashCode();
        };
    };

    public CircuitDeployerApplicationRecipe(ItemApplicationRecipeParams params) {
        this(params, true);
    };

    private CircuitDeployerApplicationRecipe(ItemApplicationRecipeParams params, boolean example) {
        super(params);
        this.example = example;
    };

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> exampleIngredients = NonNullList.of(Ingredient.EMPTY, ingredients.toArray(new Ingredient[ingredients.size()]));
        exampleIngredients.set(1, new ExampleMaskIngredient(ingredients.get(1)).toVanilla());
        return exampleIngredients;
    };

    @Override
    public Ingredient getRequiredHeldItem() {
        return new ExampleMaskIngredient(ingredients.get(1)).toVanilla();
    };

    @Override
    public List<ProcessingOutput> getRollableResults() {
        if (!example) return super.getRollableResults();
        return super.getRollableResults().stream().map(output -> {
            ItemStack result = output.getStack().copy();
            if (!(result.getItem() instanceof CircuitPatternItem)) return output;
            CircuitPatternItem.putPattern(result, EXAMPLE_PATTERN);
            return new ProcessingOutput(result, output.getChance());
        }).toList();
    };

    /**
     * @param id The ID of this recipe's holder (1.20.1 recipes knew their own ID)
     */
    public RecipeHolder<DeployerApplicationRecipe> specify(ResourceLocation id, RecipeWrapper inv) {
        ResourceLocation specificId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + recipeId++);
        DeployerApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(params -> new SpecificCircuitDeployerApplicationRecipe(params, inv), specificId)
            .withItemIngredients(ingredients)
            .withItemOutputs(results)
            .build();

        Supplier<ItemStack> result = ((ProcessingRecipeAccessor)this).getForcedResult();
        if (result != null) {
            int pattern = CircuitPatternItem.getPattern(inv.getItem(1));
            recipe.enforceNextResult(() -> SpecificCircuitDeployerApplicationRecipe.transform(result.get(), pattern));
        };
        return new RecipeHolder<>(specificId, recipe);
    };

    protected class SpecificCircuitDeployerApplicationRecipe extends CircuitDeployerApplicationRecipe {

        public SpecificCircuitDeployerApplicationRecipe(ItemApplicationRecipeParams params, RecipeWrapper inv) {
            super(params, false);
            int pattern = CircuitPatternItem.getPattern(inv.getItem(1));
            List<ProcessingOutput> specificOutputs = new ArrayList<>(results.size());

            for (Iterator<ProcessingOutput> iterator = results.iterator(); iterator.hasNext();) {
                ProcessingOutput output = iterator.next();
                ItemStack result = output.getStack();
                if (result.getItem() instanceof CircuitPatternItem || result.getItem() instanceof SequencedAssemblyItem) {
                    specificOutputs.add(new ProcessingOutput(transform(result, pattern), output.getChance()));
                    iterator.remove();
                };
            };
            results.addAll(specificOutputs);
        };

        public static ItemStack transform(ItemStack stack, int pattern) {
            ItemStack newStack = stack.copy();
            CircuitPatternItem.putPattern(newStack, pattern);
            return newStack;
        };

    };

    @SubscribeEvent
    public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        RecipeWrapper inv = event.getInventory();

        // Circuit deployer application
        if (IntStream.range(0, inv.size()).mapToObj(inv::getItem).anyMatch(stack -> stack.getItem() instanceof CircuitPatternItem)) { // Was Container.hasAnyMatching
            RecipeHolder<? extends Recipe<?>> recipe = event.getRecipe() != null && event.getRecipe().value() instanceof CircuitDeployerApplicationRecipe ? event.getRecipe() : null;
            if (recipe == null) recipe = DestroyRecipeTypes.CIRCUIT_DEPLOYING.find(event.getInventory(), event.getBlockEntity().getLevel()).orElse(null);
            if (recipe == null) recipe = SequencedAssemblyRecipe.getRecipe(event.getBlockEntity().getLevel(), event.getInventory(), DestroyRecipeTypes.CIRCUIT_DEPLOYING.<RecipeWrapper, CircuitDeployerApplicationRecipe>getType(), CircuitDeployerApplicationRecipe.class).orElse(null);
            if (recipe != null && recipe.value() instanceof CircuitDeployerApplicationRecipe circuitRecipe) {
                ResourceLocation id = recipe.id();
                event.addRecipe(() -> Optional.of(circuitRecipe.specify(id, inv)), 150);
            };
        };
    };

};
