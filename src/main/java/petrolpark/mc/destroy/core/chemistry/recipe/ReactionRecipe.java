package petrolpark.mc.destroy.core.chemistry.recipe;

import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipeParams;
import petrolpark.mc.library.compat.create.core.data.recipe.AdvancedProcessingRecipe;
import javax.annotation.Nullable;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class ReactionRecipe extends AdvancedProcessingRecipe<RecipeWrapper> {

    private static int counter;

    protected LegacyReaction reaction; // The Reaction associated with this Recipe

    public ReactionRecipe(AdvancedProcessingRecipeParams params) {
        super(DestroyRecipeTypes.REACTION, params);
    };

    public static ReactionRecipe create(LegacyReaction reaction) {
        ReactionRecipe recipe = new AdvancedProcessingRecipe.Builder<>(ReactionRecipe::new, Destroy.asResource("reaction_"+counter++)).build();
        recipe.reaction = reaction;
        return recipe;
    };

    public LegacyReaction getReaction() {
        return reaction;
    };

    @Override
    public boolean matches(RecipeWrapper pContainer, Level level) {
        return false;
    };

    @Override
    protected int getMaxInputCount() {
        return 0;
    };

    @Override
    protected int getMaxOutputCount() {
        return 0;
    };

    public static class GenericReactionRecipe extends ReactionRecipe {

        protected GenericReaction genericReaction;

        public GenericReactionRecipe(AdvancedProcessingRecipeParams params) {
            super(params);
        };

        @Nullable
        public static GenericReactionRecipe create(GenericReaction genericReaction) {
            try {
                GenericReactionRecipe recipe = new AdvancedProcessingRecipe.Builder<>(GenericReactionRecipe::new, Destroy.asResource("generic_reaction_"+counter++)).build();
                recipe.reaction = genericReaction.getExampleReaction();
                recipe.genericReaction = genericReaction;
                return recipe;
            } catch (IllegalStateException e) {
                return null; // If this fails (which it sometimes does for unfathomable reasons), then we still want the rest of the JEI plugin to load
            }

        };

        public GenericReaction getGenericReaction() {
            return genericReaction;
        };
    };
     
};
