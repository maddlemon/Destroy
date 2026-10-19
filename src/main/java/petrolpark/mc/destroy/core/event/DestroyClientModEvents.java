package petrolpark.mc.destroy.core.event;

import petrolpark.mc.library.core.world.item.decay.DecayingItemDecorator;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.chemistry.naming.SaltNameOverrides;
import petrolpark.mc.destroy.content.processing.glassblowing.BlowpipeItemRenderLayer;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItemModel;
import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternTooltipComponent;
import petrolpark.mc.destroy.content.product.periodictable.TankPeriodicTableBlockColor;
import petrolpark.mc.destroy.content.product.periodictable.TankPeriodicTableBlockItemColor;
import petrolpark.mc.destroy.core.chemistry.MoleculeDisplayItem.MoleculeTooltip;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.DyeableMixedExplosiveBlockColor;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.DyeableMixedExplosiveItemColor;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosivePropertiesTooltip;
import petrolpark.mc.destroy.core.pollution.SmogAffectedBlockColor;
import petrolpark.mc.destroy.util.NameLists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Destroy.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DestroyClientModEvents {

    @SubscribeEvent
    public static final void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(SaltNameOverrides.RELOAD_LISTENER);
        event.registerReloadListener(NameLists.RELOAD_LISTENER);
    };

    @SubscribeEvent
    public static final void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(DestroyItems.SODIUM_INGOT, new DecayingItemDecorator());
        event.register(DestroyItems.QUICKLIME, new DecayingItemDecorator());
    };

    @SubscribeEvent
    public static final void changeItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(TankPeriodicTableBlockItemColor.INSTANCE, DestroyBlocks.HYDROGEN_PERIODIC_TABLE_BLOCK, DestroyBlocks.NITROGEN_PERIODIC_TABLE_BLOCK, DestroyBlocks.OXYGEN_PERIODIC_TABLE_BLOCK, DestroyBlocks.FLUORINE_PERIODIC_TABLE_BLOCK, DestroyBlocks.CHLORINE_PERIODIC_TABLE_BLOCK, DestroyBlocks.MERCURY_PERIODIC_TABLE_BLOCK);
        event.register(DyeableMixedExplosiveItemColor.INSTANCE, DestroyBlocks.CUSTOM_EXPLOSIVE_MIX);
    };

    /**
     * Override all the color generators to account for the {@link petrolpark.mc.destroy.core.pollution.Pollution.PollutionType smog level}.
     * @param event
     */
    @SubscribeEvent
    public static final void changeBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(TankPeriodicTableBlockColor.INSTANCE, DestroyBlocks.HYDROGEN_PERIODIC_TABLE_BLOCK.get(), DestroyBlocks.NITROGEN_PERIODIC_TABLE_BLOCK.get(), DestroyBlocks.OXYGEN_PERIODIC_TABLE_BLOCK.get(), DestroyBlocks.FLUORINE_PERIODIC_TABLE_BLOCK.get(), DestroyBlocks.CHLORINE_PERIODIC_TABLE_BLOCK.get(), DestroyBlocks.MERCURY_PERIODIC_TABLE_BLOCK.get());
        event.register(DyeableMixedExplosiveBlockColor.INSTANCE, DestroyBlocks.CUSTOM_EXPLOSIVE_MIX.get());
        event.register(SmogAffectedBlockColor.GRASS, Blocks.GRASS, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.TALL_GRASS);
        event.register(SmogAffectedBlockColor.DOUBLE_TALL_GRASS, Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        event.register(SmogAffectedBlockColor.PINK_PETALS, Blocks.PINK_PETALS);
        event.register(SmogAffectedBlockColor.FOLIAGE, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
        event.register(SmogAffectedBlockColor.BIRCH, Blocks.BIRCH_LEAVES);
        event.register(SmogAffectedBlockColor.SPRUCE, Blocks.SPRUCE_LEAVES);
        event.register(SmogAffectedBlockColor.WATER, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
        event.register(SmogAffectedBlockColor.SUGAR_CANE, Blocks.SUGAR_CANE);
    };

    @SubscribeEvent
    public static final void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(MoleculeTooltip.class, MoleculeTooltip::getClientTooltipComponent);
        event.register(CircuitPatternTooltipComponent.class, CircuitPatternTooltipComponent::getClientTooltipComponent);
        event.register(ExplosivePropertiesTooltip.class, ExplosivePropertiesTooltip::getClientTooltipComponent);
    };

    @SubscribeEvent
    public static final void onRegisterModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("circuit_pattern", CircuitPatternItemModel.Loader.INSTANCE);
    };

    @SubscribeEvent
    public static final void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        BlowpipeItemRenderLayer.registerOnAll(dispatcher);
    };

};

