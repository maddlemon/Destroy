package petrolpark.mc.destroy.chemistry.api.atom;

import petrolpark.mc.destroy.chemistry.api.nuclide.IElement;
import petrolpark.mc.destroy.chemistry.api.nuclide.INuclide;
import petrolpark.mc.destroy.chemistry.api.property.ICharge;
import petrolpark.mc.destroy.chemistry.api.species.ISpecies;
import petrolpark.mc.destroy.chemistry.api.property.IElectronegativity;
import petrolpark.mc.destroy.chemistry.api.property.IRelativeAtomicMass;

/**
 * An object which can be found in chemical {@link ISpecies Species}.
 * Two individual atoms of oxygen for example, would be considered two different {@link IAtom} instances, though both might have the same {@link INuclide} (element).
 * @since Destroy 0.1.0
 * @author petrolpark
 * @see SimpleAtom Default implementation
 */
public interface IAtom<N extends INuclide> extends IRelativeAtomicMass, ICharge, IElectronegativity {
    
    /**
     * Get the {@link INuclide} of which this atom is an instance.
     * @return A nuclide (typically an {@link petrolpark.mc.destroy.chemistry.api.nuclide.ElementAveragedNuclide AveragedElement})
     */
    public N getNuclide();

    /**
     * Get the {@link IRelativeAtomicMass relative atomic mass} of this atom.
     * This is usually just the mass of the {@link IAtom#getNuclide() Nuclide} of which this atom is an instance.
     */
    @Override
    public default double getMass() {
        return getNuclide().getMass();
    };

    /**
     * Get the {@link IElement} of which this atom is an instance.
     * @return A nuclide (typically an {@link petrolpark.mc.destroy.chemistry.api.nuclide.ElementAveragedNuclide AveragedElement})
     */
    public default IElement getElement() {
        return getNuclide().getElement();
    };

};
