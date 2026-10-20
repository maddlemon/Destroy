package petrolpark.mc.destroy.content.processing.treetap;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class TreeTapBlockEntity extends BlockBreakingKineticBlockEntity {

    protected IFluidHandler fluidCapability;
	public GeniusFluidTankBehaviour tank;

    protected DestroyAdvancementBehaviour advancementBehaviour;

    protected BlockTapping currentTapping;

    public TreeTapBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    };

    @Override
    protected BlockPos getBreakingPos() {
        return getBlockPos().relative(getBlockState().getValue(TreeTapBlock.HORIZONTAL_FACING)).above();
    };

    @Override
    public boolean canBreak(BlockState stateToBreak, float blockHardness) {
        if (!super.canBreak(stateToBreak, blockHardness)) return false;
        if (currentTapping == null || !currentTapping.tappable.test(stateToBreak)) {
            currentTapping = null;
            for (BlockTapping tapping : BlockTapping.ALL_TAPPINGS) {
                if (tapping.tappable.test(stateToBreak)) currentTapping = tapping;
                break;
            };
        };
        return currentTapping != null && tank.getPrimaryHandler().fill(currentTapping.result, FluidAction.SIMULATE) > 0;
    };

    @Override
    public void onBlockBroken(BlockState stateToBreak) {
        BlockHelper.destroyBlock(level, breakingPos, 1f, stack -> {}); // Don't drop items
        tank.getPrimaryHandler().fill(currentTapping.result, FluidAction.EXECUTE);
        advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.TAP_TREE);
    };

    @Override
    protected float getBreakSpeed() {
        return super.getBreakSpeed() / 32f;
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        tank = new GeniusFluidTankBehaviour(GeniusFluidTankBehaviour.TYPE, this, 1, getCapacity(), false);
        tank.forbidInsertion();
        behaviours.add(tank);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.TAP_TREE);
        behaviours.add(advancementBehaviour);

        refreshCapability();
    };

    private void refreshCapability() {
        fluidCapability = tank.getPrimaryHandler();
        invalidateCapabilities();
    };

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (fluidCapability == null) return false;
        return containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability);
    };

    public IFluidHandler getFluidHandler(Direction side) {
        if (side != null && side != getBlockState().getValue(TreeTapBlock.HORIZONTAL_FACING).getOpposite()) return null;
        if (fluidCapability == null) refreshCapability();
        return fluidCapability;
    };

    public int getCapacity() {
        return DestroyAllConfigs.SERVER.blocks.treeTapCapacity.get();
    };
    
};
