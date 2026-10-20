package petrolpark.mc.destroy.mixin.compat.farmersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

/**
 * Cutting onions on a Cutting Board makes everyone nearby cry.
 * <p>1.20.1 injected before {@code CuttingBoardBlock#spawnCuttingParticles} in {@code use}. In Farmer's Delight 1.21 cutting happens in
 * {@link CuttingBoardBlockEntity#processStoredItemUsingTool} (called from {@code useItemOn}), so this wraps that call: same trigger
 * (a Player successfully cutting the Item on the board), same effect.</p>
 */
@Mixin(CuttingBoardBlock.class)
public class CuttingBoardMixin {

    @WrapOperation(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lvectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity;processStoredItemUsingTool(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    public boolean wrapProcessStoredItemUsingTool(CuttingBoardBlockEntity cuttingBoardEntity, ItemStack tool, Player player, Operation<Boolean> original, ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player usingPlayer, InteractionHand hand, BlockHitResult hit) {
        ItemStack boardStack = cuttingBoardEntity.getStoredItem().copy(); // The cut removes the Item, so remember it first
        boolean cut = original.call(cuttingBoardEntity, tool, player);
        if (cut && DestroyAllConfigs.SERVER.compat.cuttingOnionsCausesCrying.get() && boardStack.is(AllTags.commonItemTag("crops/onion"))) {
            DestroyAdvancementTrigger.CUT_ONIONS.award(level, player);
            level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(2f)).forEach(entity -> entity.addEffect(new MobEffectInstance(DestroyMobEffects.CRYING, 400, 0, false, false, true)));
        };
        return cut;
    };
};
