package petrolpark.mc.destroy.core.chemistry.storage;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.util.DestroyLang;
import petrolpark.mc.destroy.content.product.periodictable.ElementTankFillingRecipe;
import petrolpark.mc.destroy.core.block.entity.IHaveLabGoggleInformation;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import com.simibubi.create.foundation.recipe.RecipeFinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;

public class ElementTankBlockEntity extends SmartBlockEntity implements IHaveLabGoggleInformation {

    protected SmartFluidTankBehaviour tank;
    protected PollutingBehaviour pollutingBehaviour;

    protected final Object recipeCacheKey = new Object();

    public ElementTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new GeniusFluidTankBehaviour(GeniusFluidTankBehaviour.TYPE, this, 1, 1000, false)
            .whenFluidUpdates(this::checkRecipe);
        behaviours.add(tank);

        pollutingBehaviour = new PollutingBehaviour(this);
        behaviours.add(pollutingBehaviour);
    };

    public void checkRecipe() {
        FluidStack fs = tank.getPrimaryHandler().getFluid();
        RecipeFinder.get(recipeCacheKey, getLevel(), r -> r.value().getType() == DestroyRecipeTypes.ELEMENT_TANK_FILLING.getType()).stream().map(r -> (ElementTankFillingRecipe)r.value()).filter(r -> {
            SizedFluidIngredient ingredient = r.getFluidIngredients().get(0);
            return ingredient.test(fs) && ingredient.amount() <= fs.getAmount();
        }).findFirst().ifPresent(r -> {
            BlockState state = r.blockResult.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            getLevel().setBlockAndUpdate(getBlockPos(), state);
        });
    };

    public IFluidHandler getFluidHandler(Direction side) {
        return tank.getCapability();
    };

    public FluidStack getRenderedFluid() {
        return tank.getPrimaryTank().getRenderedFluid();
    };

    public float getFluidLevel(float partialTicks) {
        return tank.getPrimaryTank().getFluidLevel().getValue(partialTicks);
    };

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        DestroyLang.tankInfoTooltip(tooltip, DestroyLang.builder().add(Component.translatable("block.destroy.element_tank")), tank.getPrimaryHandler());
        return true;
    };
    
};
