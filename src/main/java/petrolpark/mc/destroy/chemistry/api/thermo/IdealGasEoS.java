package petrolpark.mc.destroy.chemistry.api.thermo;

import petrolpark.mc.destroy.chemistry.api.util.Constants;

/**
 * The Ideal Gas Equation of State.
 * @since Destroy 0.1.1
 * @author petrolpark
 */
public class IdealGasEoS implements IEoS {

    @Override
    public double getPressure(double T, double Vm) {
        return T * Constants.GAS_CONSTANT / Vm;
    };
    
};
