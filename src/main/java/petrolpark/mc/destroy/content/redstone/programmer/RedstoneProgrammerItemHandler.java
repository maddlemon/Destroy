package petrolpark.mc.destroy.content.redstone.programmer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.content.redstone.programmer.RedstoneProgrammerBlockItem.ItemStackRedstoneProgram;

import net.createmod.catnip.data.WorldAttached;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class RedstoneProgrammerItemHandler {

    public static final WorldAttached<Map<UUID, ItemStackRedstoneProgram>> programs = new WorldAttached<>(level -> new HashMap<>());
    public static final int TIMEOUT = 30;

    public static void tick(LevelAccessor level) {
        Map<UUID, ItemStackRedstoneProgram> map = programs.get(level);
        for (Iterator<Entry<UUID, ItemStackRedstoneProgram>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Entry<UUID, ItemStackRedstoneProgram> entry = iterator.next();
            ItemStackRedstoneProgram program = entry.getValue();
            program.ttl--;
            if (!program.shouldTransmit()) {
                program.unload();
                iterator.remove();
            };
        };
    };

    @SubscribeEvent
    public static final void onLevelTickPre(LevelTickEvent.Pre event) {
        tick(event.getLevel());
    };

    // Forge fired this tick event at both START and END and the handler ignored the phase, so it ran twice per tick. Kept that way to match 1.20.1.
    @SubscribeEvent
    public static final void onLevelTickPost(LevelTickEvent.Post event) {
        tick(event.getLevel());
    };
    
};
