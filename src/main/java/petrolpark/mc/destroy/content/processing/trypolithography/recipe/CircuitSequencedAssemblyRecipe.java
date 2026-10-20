package petrolpark.mc.destroy.content.processing.trypolithography.recipe;

import java.util.function.Function;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;
import petrolpark.mc.destroy.util.DestroyIngredientHelper;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeSerializer;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class CircuitSequencedAssemblyRecipe extends SequencedAssemblyRecipe {

    public CircuitSequencedAssemblyRecipe(SequencedAssemblyRecipeSerializer serializer) {
        super(serializer);
    };

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack stack = resultPool.get(0).getStack().copy();
        CircuitPatternItem.putPattern(stack, CircuitDeployerApplicationRecipe.EXAMPLE_PATTERN);
        return stack;
    };

    public static class Serializer extends SequencedAssemblyRecipeSerializer {

        private final MapCodec<SequencedAssemblyRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> streamCodec;

        public Serializer() {
            super();
            codec = super.codec().flatXmap(recipe -> validate(copy(recipe)), DataResult::success);
            streamCodec = super.streamCodec().map(this::copy, Function.identity());
        };

        @Override
        public MapCodec<SequencedAssemblyRecipe> codec() {
            return codec;
        };

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SequencedAssemblyRecipe> streamCodec() {
            return streamCodec;
        };

        private static DataResult<SequencedAssemblyRecipe> validate(SequencedAssemblyRecipe recipe) {
            boolean inputHasPattern = !DestroyIngredientHelper.parseTimeItems(recipe.getIngredient()).anyMatch(stack -> !(stack.getItem() instanceof CircuitPatternItem));
            if (!inputHasPattern && recipe.getLoops() != 1) return DataResult.error(() -> "Circuit Sequenced Assembly recipes may not loop (unless the Circuit Pattern is determined by the input item), as it makes ambigious the step at which the Circuit Pattern of the result is determined.");
            boolean foundConferringRecipe = inputHasPattern;
            for (SequencedRecipe<?> subRecipe : recipe.getSequence()) {
                if (subRecipe.getRecipe() instanceof IConfersCircuitPatternRecipe) {
                    if (foundConferringRecipe) {
                        return DataResult.error(() -> "Circuit Sequenced Assembly recipes may only define a single sub-recipe that can determine the Circuit Pattern of the result.");
                    } else {
                        foundConferringRecipe = true;
                    };
                };
            };
            if (!foundConferringRecipe) return DataResult.error(() -> "Circuit Sequenced Assembly recipes must define a step in which the Circuit Pattern of the result gets determined.");
            return DataResult.success(recipe);
        };

        public CircuitSequencedAssemblyRecipe copy(SequencedAssemblyRecipe wrappedRecipe) {
            CircuitSequencedAssemblyRecipe recipe = new CircuitSequencedAssemblyRecipe(this);
            recipe.ingredient = wrappedRecipe.getIngredient();
            recipe.sequence = wrappedRecipe.getSequence();
            recipe.loops = wrappedRecipe.getLoops();
            recipe.transitionalItem = new ProcessingOutput(wrappedRecipe.getTransitionalItem(), 1);
            recipe.resultPool.addAll(wrappedRecipe.resultPool);
            return recipe;
        };
    };
};
