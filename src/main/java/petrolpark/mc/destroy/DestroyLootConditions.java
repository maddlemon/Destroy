package petrolpark.mc.destroy;

import petrolpark.mc.destroy.core.explosion.ObliterationCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DestroyLootConditions {

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Destroy.MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType>
    OBLITERATION = registerLootCondition("obliteration", new ObliterationCondition.Serializer());

    private static DeferredHolder<LootItemConditionType, LootItemConditionType> registerLootCondition(String name, Serializer<? extends LootItemCondition> serializer) {
        return LOOT_CONDITIONS.register(name, () -> new LootItemConditionType(serializer));
    };

    public static void register(IEventBus eventBus) {
        LOOT_CONDITIONS.register(eventBus);
    };
};
