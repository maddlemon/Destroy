package petrolpark.mc.destroy.core.pollution;

import petrolpark.mc.destroy.DestroyCapabilities;
import java.util.ArrayList;
import java.util.List;

import petrolpark.mc.destroy.Destroy;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Behaviour for Tile Entities which contain Fluids and should release those Fluids into the atmosphere if destroyed.
 */
@EventBusSubscriber(modid = Destroy.MOD_ID)
public class PollutingBehaviour extends BlockEntityBehaviour {

    public static BehaviourType<PollutingBehaviour> TYPE = new BehaviourType<>();

    public PollutingBehaviour(SmartBlockEntity be) {
        super(be);
    };

    @Override
    public void destroy() {
        List<FluidStack> fluidsToRelease = new ArrayList<>();
        IFluidHandler availableFluids = DestroyCapabilities.fluidHandler(blockEntity, null);
        if (availableFluids == null) return;
        for (int tankNo = 0; tankNo < availableFluids.getTanks(); tankNo++) {
            FluidStack fluidStack = availableFluids.getFluidInTank(tankNo);
            if (fluidStack.isEmpty()) continue;
            fluidsToRelease.add(fluidStack);
        };

        PollutionHelper.pollute(getWorld(), getPos(), fluidsToRelease.toArray((i) -> new FluidStack[i]));

        super.destroy();
    };

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    };

    /**
     * 1.20.1 attached this to Basins, Item Drains and Spouts with one generic event each; Create 6 has one event for all Smart Block Entities.
     */
    @SubscribeEvent
    public static void onAttachBehaviours(BlockEntityBehaviourEvent event) {
        event.forType(AllBlockEntityTypes.BASIN.get(), be -> event.attach(new PollutingBehaviour(be)));
        event.forType(AllBlockEntityTypes.ITEM_DRAIN.get(), be -> event.attach(new PollutingBehaviour(be)));
        event.forType(AllBlockEntityTypes.SPOUT.get(), be -> event.attach(new PollutingBehaviour(be)));
    };
    
};
