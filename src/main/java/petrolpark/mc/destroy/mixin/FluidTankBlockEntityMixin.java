package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour.GeniusFluidTank;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.neoforged.neoforge.fluids.FluidStack;

@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin {
    
    @Overwrite(remap = false)
    protected SmartFluidTank createInventory() {
        return new GeniusFluidTank(FluidTankBlockEntity.getCapacityMultiplier(), this::invokeOnFluidStackChanged);
    };

    @Invoker(
        value = "onFluidStackChanged",
        remap = false
    )
    public abstract void invokeOnFluidStackChanged(FluidStack stack);
};
