package petrolpark.mc.destroy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItem;
import petrolpark.mc.destroy.content.processing.trypolithography.recipe.CircuitSequencedAssemblyRecipe;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

@Mixin(SequencedAssemblyRecipe.class)
public abstract class SequencedAssemblyRecipeMixin implements Recipe<RecipeWrapper> {

    @Shadow(remap = false)
    private int getStep(ItemStack input) {
        throw new AssertionError();
    };

    @Shadow(remap = false)
    private ItemStack rollResult(RandomSource random) {
        throw new AssertionError();
    };

    @Shadow(remap = false)
    protected List<SequencedRecipe<?>> sequence;

    @Shadow(remap = false)
    protected int loops;

    @Shadow(remap = false)
    public abstract ItemStack getTransitionalItem();

    /**
     * Mostly copied from {@link com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe#advance Create source code}.
     * @param id The ID of this Sequenced Assembly recipe
     * @param input The stack being processed
     * @param random
     * @return The result of processing, which, unlike the default behaviour, conserves other data components
     */
    @Overwrite(remap = false)
    private ItemStack advance(ResourceLocation id, ItemStack input, RandomSource random) {
        int step = getStep(input);
        ItemStack result;
		if ((step + 1) / sequence.size() >= loops) {
            result = rollResult(random);
        } else {
            result = step == 0 ? getTransitionalItem().copyWithCount(1) : input.copyWithCount(1);
            result.set(AllDataComponents.SEQUENCED_ASSEMBLY, new SequencedAssemblyRecipe.SequencedAssembly(id, step + 1, (step + 1f) / (sequence.size() * loops)));
        };

        if (getSerializer() instanceof CircuitSequencedAssemblyRecipe.Serializer) {
            CircuitPatternItem.putPattern(result, CircuitPatternItem.getPattern(input));
        };

		return result;
    };
};
