package petrolpark.mc.destroy.core.chemistry.vat.ponder;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class FillVatPonderInstruction extends PonderInstruction {

    public final BlockPos vatControllerPos;
    public final FluidStack fillStack;

    public FillVatPonderInstruction(BlockPos vatControllerPos, FluidStack fillStack) {
        this.vatControllerPos = vatControllerPos;
        this.fillStack = fillStack;
    };

    @Override
    public boolean isComplete() {
        return true;
    };

    @Override
    public void tick(PonderScene scene) {
        scene.getWorld().getBlockEntity(vatControllerPos, DestroyBlockEntityTypes.VAT_CONTROLLER.get()).ifPresent(vat ->
            vat.addFluid(fillStack, FluidAction.SIMULATE)
        );
    };
    
};
