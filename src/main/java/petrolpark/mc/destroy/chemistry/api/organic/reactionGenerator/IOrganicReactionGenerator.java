package petrolpark.mc.destroy.chemistry.api.organic.reactionGenerator;

import petrolpark.mc.destroy.chemistry.api.organic.IFunctionalGroup;
import petrolpark.mc.destroy.chemistry.api.organic.IFunctionalGroupInstance;
import petrolpark.mc.destroy.chemistry.api.registry.IRegisteredChemistryObject;
import petrolpark.mc.destroy.chemistry.api.species.ISpecies;
import petrolpark.mc.destroy.chemistry.api.species.NamespacedId;
import petrolpark.mc.destroy.chemistry.api.transformation.reaction.IReaction;

/**
 * A generator for {@link IReaction}s involving one or more {@link IFunctionalGroup}s.
 * These take in {@link ISpecies} with {@link IFunctionalGroupInstance instances of those Functional Groups} and generates an {@link IReaction}.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface IOrganicReactionGenerator<OR extends IOrganicReactionGenerator<OR>> extends IRegisteredChemistryObject<OR, NamespacedId> {

};
