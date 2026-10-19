package petrolpark.mc.destroy.chemistry.api.organic.reactionGenerator;

import petrolpark.mc.destroy.chemistry.api.organic.IFunctionalGroup;
import petrolpark.mc.destroy.chemistry.api.organic.IFunctionalGroupInstance;
import petrolpark.mc.destroy.chemistry.api.organic.IOrganicReactant;
import petrolpark.mc.destroy.chemistry.api.species.ISpecies;
import petrolpark.mc.destroy.chemistry.api.transformation.reaction.IChemicalReaction;
import petrolpark.mc.destroy.chemistry.api.transformation.reaction.IReaction;

/**
 * An {@link IOrganicReactionGenerator} which generates {@link IReaction}s involving a single {@link IFunctionalGroup}.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface ISingleGroupOrganicReactionGenerator<G extends IFunctionalGroup<? super G>> extends IOrganicReactionGenerator<ISingleGroupOrganicReactionGenerator<G>> {
    
    /**
     * Generate the organic {@link IChemicalReaction} involving a specific {@link ISpecies}.
     * @param reactant The reacting {@link IOrganicReactant}, which will contain an {@link IFunctionalGroupInstance instance of} the {@link IFunctionalGroup} of this {@link ISingleGroupOrganicReactionGenerator}
     * @return An {@link IChemicalReaction} with the {@link IOrganicReactant#getSpecies() Species} of the {@link IOrganicReactant} as a reactant.
     * @since Destroy 0.1.0
     * @author petrolpark
     */
    public IChemicalReaction generate(IOrganicReactant<G> reactant);
};
