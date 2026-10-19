package petrolpark.mc.destroy;

import petrolpark.mc.destroy.core.explosion.SmartExplosion;

import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class DestroyLootContextParams {

    public static final LootContextParam<SmartExplosion> SMART_EXPLOSION = create("smart_explosion");
    
    private static <T> LootContextParam<T> create(String id) {
        return new LootContextParam<>(Destroy.asResource(id));
    };
};
