package petrolpark.mc.destroy.compat.createbigcannons.item;

import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CustomExplosiveMixShellBlockEntity; // TODO: CBC
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.DyeableMixedExplosiveBlockItem;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;

import net.minecraft.world.level.block.Block;

public class CustomExplosiveMixShellBlockItem extends DyeableMixedExplosiveBlockItem {

    public CustomExplosiveMixShellBlockItem(Block block, Properties properties) {
        super(block, properties);
    };

    @Override
    public int getExplosiveInventorySize() {
        return DestroyAllConfigs.SERVER.compat.customExplosiveMixShellSize.get();
    };

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return CustomExplosiveMixShellBlockEntity.EXPLOSIVE_PROPERTY_CONDITIONS;
    };
    
};