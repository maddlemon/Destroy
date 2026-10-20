package petrolpark.mc.destroy.core.block.entity;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public interface IDirectionalOutputFluidBlockEntity {

    public default boolean isTankFull(FluidTank tank) {
        return tank.getFluidAmount() == tank.getCapacity();
    };

    public default boolean canFitFluidInTank(FluidStack stack, FluidTank tank) {
        return (stack.getFluid() == tank.getFluid().getFluid() || tank.isEmpty()) && stack.getAmount() <= tank.getSpace();
    };

    public default boolean hasFluidInTank(SizedFluidIngredient ingredient, FluidTank tank) {
        if (tank.drain(ingredient.amount(), FluidAction.SIMULATE).getAmount() != ingredient.amount()) return false; //check there is enough Fluid
        for (FluidStack stack : java.util.Arrays.asList(ingredient.getFluids())) {
            if (stack.getFluid() == tank.getFluid().getFluid()) return true; //check it is the right Fluid type
        };
        return false;
    };

    /**
     * Used by the Centrifuge and Bubble Cap to the side to which they should input/output.
     * @param be The Block Entity being rotated
     * @param currentDirection The current face which the output is on
     * @param tank The Tank from which the output would be outputting
     * @param output Whether this is an output or input
     * @return The new face the output should point to
     */
    @SuppressWarnings("null")
    public default Direction refreshDirection(SmartBlockEntity be, Direction currentDirection, FluidTank tank, boolean output) {
        if (!be.hasLevel() || currentDirection.getAxis() == Direction.Axis.Y) { // If the level doesn't exist (low-key no idea how this error even occured), or the side is UP or DOWN, fix this
            return Direction.NORTH;
        };
        Direction direction = currentDirection;
        int i = 0;
        while (i < 4) { // Loop through possible Directions, prioritising the current Direction
            BlockEntity adjacentBE = be.getLevel().getBlockEntity(be.getBlockPos().relative(direction)); // It thinks 'level' can be null (it can't)
            BlockState adjacentState = be.getLevel().getBlockState(be.getBlockPos().relative(direction));
            if (adjacentBE != null) {
                FluidTransportBehaviour transport = BlockEntityBehaviour.get(adjacentBE, FluidTransportBehaviour.TYPE);
                if (transport != null) {
                    if (output && transport.canPullFluidFrom(tank.getFluid(), adjacentState, direction.getOpposite())) { // If Fluid can be outputted in this Direction
                        return direction;
                    } else if (!output && transport.canHaveFlowToward(adjacentState, direction.getOpposite())) { // If Fluid can be inserted from this Direction
                        return direction;
                    } else if (FluidPipeBlock.isPipe(adjacentState) || (adjacentState.getBlock() instanceof GlassFluidPipeBlock && direction.getAxis() != adjacentState.getValue(RotatedPillarBlock.AXIS))) {
                        return direction;
                    };
                } else {
                    
                }
            };
            direction = direction.getClockWise(); // Check the next Direction
            i++;
        };

        return currentDirection; // If no other Direction was found, keep it the way it was
    };
}
