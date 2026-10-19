package petrolpark.mc.destroy.compat.jei.category;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe.GenericReactionRecipe;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

public class GenericReactionCategory extends ReactionCategory<GenericReactionRecipe> {

    public static RecipeType<GenericReactionRecipe> TYPE;

    /**
     * Each {@link petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType Group Type} mapped to the Set of {@link petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction Generic Reactions} that can produce it.
     * The {@link petrolpark.mc.destroy.chemistry.legacy.LegacySpecies#isNovel novel} results of {@link petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction#getExampleReaction this Reaction} are used to
     * determine if a Generic Reaction can produce a Group.
     */
    public static Map<LegacyFunctionalGroupType<?>, Set<GenericReaction>> GROUP_RECIPES = new HashMap<>();

    /**
     * The set of all {@link petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction#getExampleReaction example} {@link petrolpark.mc.destroy.chemistry.legacy.LegacyReaction Reactions}, mapped to
     * the {@link petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction Generic Reactions} of which they are the example.
     */
    public static Map<GenericReaction, GenericReactionRecipe> RECIPES = new HashMap<>();

    public GenericReactionCategory(Info<GenericReactionRecipe> info, IJeiHelpers helpers) {
        super(info, helpers);
        GenericReactionCategory.TYPE = info.recipeType();
    };

    @Override
    protected String getTranslationKey(ReactionRecipe recipe) {
        if (recipe instanceof GenericReactionRecipe gRecipe) {
            ResourceLocation id = gRecipe.getGenericReaction().id;
            return id.getNamespace() + ".generic_reaction." + id.getPath();
        };
        return "";
    };

    /**
     * Generate every Generic Reaction's recipe to go in JEI.
     */
    static {
        for (GenericReaction genericReaction : GenericReaction.GENERIC_REACTIONS) {
            LegacyReaction reaction = null;
            try {
                reaction = genericReaction.getExampleReaction();
            } catch (Throwable e) {
                Destroy.LOGGER.warn("Problem generating generic reaction "+genericReaction.id); // Warn but don't do anything
                throw e;
            };
            if (reaction != null) {
                // Add the Generic Reaction to JEI.
                RECIPES.put(genericReaction, GenericReactionRecipe.create(genericReaction));
                for (LegacySpecies product : reaction.getProducts()) {
                    if (product.isNovel()) {
                        // Determine what functional groups this Generic Reaction can produce
                        for (LegacyFunctionalGroup<?> functionalGroup : product.getFunctionalGroups()) {
                            GROUP_RECIPES.merge(functionalGroup.getType(), Set.of(genericReaction), Sets::union);
                        };
                    };
                };
            };
        };
    };
    
};
