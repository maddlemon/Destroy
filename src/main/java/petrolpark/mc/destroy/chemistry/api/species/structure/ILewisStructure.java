package petrolpark.mc.destroy.chemistry.api.species.structure;

import petrolpark.mc.destroy.chemistry.api.registry.IRegisteredChemistryObject;

/**
 * A simplistic implementation of {@link IMolecularStructure} which models bonds as largely localized.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface ILewisStructure {

    public interface IBondType extends IRegisteredChemistryObject<IBondType, Character> {

    };
    
};
