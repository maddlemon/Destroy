package petrolpark.mc.destroy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * Jukebox Songs are data-driven in 1.21 ({@code data/destroy/jukebox_song}); these replace the arguments of 1.20.1's {@code RecordItem}s
 * (same sound, description, length and comparator output).
 */
public class DestroyJukeboxSongs {

    public static final ResourceKey<JukeboxSong>
    
    SPECTRUM = create("spectrum"),
    BLANK = create("blank");

    private static ResourceKey<JukeboxSong> create(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, Destroy.asResource(name));
    };
};
