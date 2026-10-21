package petrolpark.mc.destroy.compat.curios;

import net.neoforged.bus.api.IEventBus;
import petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper.Protection;
import petrolpark.mc.library.compat.curios.PetrolparkCurios;

public class DestroyCurios {
    
    public static void init(IEventBus modEventBus, IEventBus forgeEventBus) {

        // Effects of wearing Curios
        registerCuriosTest(Protection.HEAD, "head");
        registerCuriosTest(Protection.EYES, "head");
        registerCuriosTest(Protection.NOSE, "head");
        registerCuriosTest(Protection.MOUTH, "head");
        registerCuriosTest(Protection.MOUTH_COVERED, "head");
        // Engineer's Goggles predicate for Curios-worn goggles: registered by the library (PetrolparkCurios.ctor) for every PetrolparkCuriosSetup.goggles() item
    };

    private static void registerCuriosTest(Protection protectionType, String slotId) {
        protectionType.registerTest(PetrolparkCurios.wearingCurioPredicate(protectionType.defaultTag::matches, slotId));
    };
};
