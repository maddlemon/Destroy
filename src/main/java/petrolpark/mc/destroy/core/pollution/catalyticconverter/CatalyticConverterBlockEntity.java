package petrolpark.mc.destroy.core.pollution.catalyticconverter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class CatalyticConverterBlockEntity extends SmartBlockEntity {

    protected DestroyAdvancementBehaviour advancementBehaviour;
    protected GeniusFluidTankBehaviour tankBehaviour;
    protected int ticksToFlush;

    public CatalyticConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    };

    @Override
    public void tick() {
        super.tick();
        ticksToFlush--;
        if (ticksToFlush <= 0) {
            ticksToFlush = 10;
            if (tankBehaviour.isEmpty()) return;
            float multiplier = DestroyAllConfigs.SERVER.blocks.catalyticConverterReduction.getF();
            if (multiplier > 0f) PollutionHelper.pollute(level, getBlockPos().relative(getBlockState().getValue(CatalyticConverterBlock.FACING)), multiplier, 10, tankBehaviour.getPrimaryHandler().getFluid());
            advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.CATALYTIC_CONVERTER);
            tankBehaviour.getPrimaryHandler().drain(1000000, FluidAction.EXECUTE);
            notifyUpdate();
        };
    };

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        ticksToFlush = tag.getInt("TicksToFlush");
    };

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("TicksToFlush", ticksToFlush);
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tankBehaviour = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 1000000, false);
        tankBehaviour.forbidExtraction();
        behaviours.add(tankBehaviour);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.CATALYTIC_CONVERTER);
        behaviours.add(advancementBehaviour);
    };

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.FluidHandler.BLOCK && side == null || side == getBlockState().getValue(CatalyticConverterBlock.FACING).getOpposite()) return tankBehaviour.getCapability().cast();
        return super.getCapability(cap, side);
    };
    
};
