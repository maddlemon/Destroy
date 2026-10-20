package petrolpark.mc.destroy;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import javax.annotation.Nullable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Block Entity capabilities. In NeoForge 1.21 these are registered per Block Entity type here, rather than by
 * overriding {@code getCapability} on the Block Entity. Each Block Entity exposes typed {@code getFluidHandler(side)}
 * / {@code getItemHandler(side)} methods (returning {@code null} for "no capability on this side"), and calls
 * {@code invalidateCapabilities()} whenever what those methods return changes.
 */
@EventBusSubscriber(modid = Destroy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DestroyCapabilities {

    @SubscribeEvent
    public static final void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
        // Fluid handlers
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.AGING_BARREL.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.BUBBLE_CAP.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.CATALYTIC_CONVERTER.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.CENTRIFUGE.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.COOLER.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.ELEMENT_TANK.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.MEASURING_CYLINDER.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.PUMPJACK.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.SIMPLE_MIXTURE_TANK.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.SIPHON.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.TREE_TAP.get(), (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, DestroyBlockEntityTypes.VAT_SIDE.get(), (be, side) -> be.getFluidHandler(side));

        // Item handlers
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DestroyBlockEntityTypes.AGING_BARREL.get(), (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DestroyBlockEntityTypes.CUSTOM_EXPLOSIVE_MIX.get(), (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DestroyBlockEntityTypes.TEST_TUBE_RACK.get(), (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DestroyBlockEntityTypes.VAT_SIDE.get(), (be, side) -> be.getItemHandler(side));
    };

    /**
     * Look up the Fluid Handler a Block Entity exposes on the given side (replacement for the old
     * {@code be.getCapability(FLUID_HANDLER, side).orElse(null)}).
     */
    @Nullable
    public static IFluidHandler fluidHandler(@Nullable BlockEntity be, @Nullable Direction side) {
        if (be == null || be.getLevel() == null) return null;
        return be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, side);
    };

    /**
     * Look up the Item Handler a Block Entity exposes on the given side.
     */
    @Nullable
    public static IItemHandler itemHandler(@Nullable BlockEntity be, @Nullable Direction side) {
        if (be == null || be.getLevel() == null) return null;
        return be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, side);
    };
};
