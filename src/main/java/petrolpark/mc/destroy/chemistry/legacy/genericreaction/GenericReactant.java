package petrolpark.mc.destroy.chemistry.legacy.genericreaction;

import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;

public class GenericReactant<G extends LegacyFunctionalGroup<G>> {

    public final LegacySpecies molecule;
    public final G group;

    public GenericReactant(LegacySpecies molecule, G group) {
        this.molecule = molecule;
        this.group = group;
    };

    public LegacySpecies getMolecule() {
        return this.molecule;
    };

    public G getGroup() {
        return this.group;
    };
}
