package petrolpark.mc.destroy.chemistry.api.transformation.reaction.result;

import petrolpark.mc.destroy.chemistry.api.reactor.IReactor;
import petrolpark.mc.destroy.chemistry.api.transformation.reaction.IReaction;

/**
 * Something which comes about as a result of an {@link IReaction}, other than a {@link IMoleculeComponent} being {@link IReaction#getProduct(petrolpark.mc.destroy.chemistry.api.mixture.IMixtureComponent) produced} or {@link IHeatable heating} occuring.
 * {@link IReactionResult}s occur singly in time.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public interface IReactionResult {
    
    /**
     * 
     * @param reactor
     * @since Destroy 0.1.0
     * @author petrolpark
     */
    public void enact(IReactor reactor);
};
