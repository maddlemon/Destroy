package petrolpark.mc.destroy.chemistry.forge.event;

import petrolpark.mc.destroy.chemistry.api.event.IChemistryEvent;
import petrolpark.mc.destroy.chemistry.api.event.IChemistryEventFirer;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

/**
 * The Minecraft Forge implementation of {@link IChemistryEventFirer}.
 * {@link IChemistryEvent}s are fired as Forge {@link Event}s.
 * @since Destroy 0.1.0
 * @author petrolpark
 */
public class ForgeChemistryEventFirer implements IChemistryEventFirer {

    @Override
    public <E extends IChemistryEvent> ForgeChemistryEvent<E> fire(E chemistryEvent) {
        ForgeChemistryEvent<E> forgeEvent = new ForgeChemistryEvent<E>(chemistryEvent);
        NeoForge.EVENT_BUS.post(forgeEvent);
        return forgeEvent;
    };


    
};
