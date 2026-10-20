package petrolpark.mc.destroy.core.item;

import petrolpark.mc.destroy.DestroyJukeboxSongs;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxPlayable;

/**
 * A disc that plays silence ({@link DestroyJukeboxSongs#BLANK}). As in 1.20.1 its track title is not shown in its tooltip.
 */
@MoveToPetrolparkLibrary
public class BlankRecordItem extends Item {

    public BlankRecordItem(Properties properties) {
        super(properties.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(DestroyJukeboxSongs.BLANK), false)));
    };
    
};
