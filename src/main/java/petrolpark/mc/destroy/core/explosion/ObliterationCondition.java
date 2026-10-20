package petrolpark.mc.destroy.core.explosion;

import com.mojang.serialization.MapCodec;
import petrolpark.mc.destroy.DestroyLootConditions;
import petrolpark.mc.destroy.DestroyLootContextParams;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class ObliterationCondition implements LootItemCondition {

    private static final ObliterationCondition INSTANCE = new ObliterationCondition();

    public static final MapCodec<ObliterationCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean test(LootContext context) {
        SmartExplosion explosion = context.getParamOrNull(DestroyLootContextParams.SMART_EXPLOSION);
        if (explosion != null) {
            return explosion.shouldDoObliterationDrops();
        } else {
            return false;
        }
    };

    @Override
    public LootItemConditionType getType() {
        return DestroyLootConditions.OBLITERATION.get();
    };

};
