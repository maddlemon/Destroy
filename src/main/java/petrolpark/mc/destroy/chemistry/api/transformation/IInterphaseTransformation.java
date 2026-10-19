package petrolpark.mc.destroy.chemistry.api.transformation;

import petrolpark.mc.destroy.chemistry.api.mixture.IMixtureComponent;
import petrolpark.mc.destroy.chemistry.api.mixture.IPhase;
import petrolpark.mc.destroy.chemistry.api.transformation.reaction.IReacting;

/**
 * A {@link ITransformation} which takes place at the interface of two {@link IPhase}s.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface IInterphaseTransformation<P extends IPhase<? super C>, C extends IMixtureComponent, R extends IReacting<? super R>> extends ITransformation<R> {
    
};
