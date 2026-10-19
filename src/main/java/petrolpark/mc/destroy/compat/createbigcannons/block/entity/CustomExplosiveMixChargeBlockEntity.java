package petrolpark.mc.destroy.compat.createbigcannons.block.entity;

import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveInventory;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.SimpleDyeableNameableMixedExplosiveBlockEntity;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CustomExplosiveMixChargeBlockEntity extends SimpleDyeableNameableMixedExplosiveBlockEntity {

    public static ExplosivePropertyCondition[] EXPLOSIVE_PROPERTY_CONDITIONS = new ExplosivePropertyCondition[]{
        ExplosiveProperties.CAN_EXPLODE
    };
    
    public CustomExplosiveMixChargeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    };

    @Override
    public MixedExplosiveInventory createInv() {
        return new MixedExplosiveInventory(DestroyAllConfigs.SERVER.compat.customExplosiveMixChargeSize.get(), EXPLOSIVE_PROPERTY_CONDITIONS);
    };

    @Override
    public void explode(@Nullable Player cause) {};

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return EXPLOSIVE_PROPERTY_CONDITIONS;
    };

    @Override
    public String getExplosivePropertyDescriptionTranslationKeySuffix() {
        return "createbigcannons_charge";
    };
    
};