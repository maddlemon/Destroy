package petrolpark.mc.destroy.chemistry.api.species;

import petrolpark.mc.destroy.chemistry.api.atom.IAtom;
import petrolpark.mc.destroy.chemistry.api.nuclide.INuclide;
import petrolpark.mc.destroy.chemistry.api.util.ImmutableObjectIntMap;

/**
 * A {@link Map} of {@link INuclide}s to a number of {@link IAtom}s of that {@link INuclide}.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface IMolecularFormula<N extends INuclide> extends ImmutableObjectIntMap<N> {
    
    /**
     * Get the number of {@link IAtom}s of this {@link INuclide} present.
     * @param nuclide
     * @return A number greater than or equal to {@code 0}.
     */
    @Override
    public int get(N nuclide);
};
