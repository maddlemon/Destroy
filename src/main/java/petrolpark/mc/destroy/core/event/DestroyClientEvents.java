package petrolpark.mc.destroy.core.event;

import com.mojang.datafixers.util.Either;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyClient;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.content.oil.seismology.SeismometerItemRenderer;
import petrolpark.mc.destroy.content.tool.swissarmyknife.SwissArmyKnifeItem;
import petrolpark.mc.destroy.core.block.entity.BlockEntityBehaviourRenderer;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosivePropertiesTooltip;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.IMixedExplosiveItem;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Destroy.MOD_ID)
public class DestroyClientEvents {

    /**
     * Tick a couple of renderers.
     * @param event
     */
    @SubscribeEvent
    public static final void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            SeismometerItemRenderer.tick();
            SwissArmyKnifeItem.clientPlayerTick();
            DestroyClient.EXTENDED_INVENTORY_HANDLER.tick(event);
        } else {
            BlockEntityBehaviourRenderer.tick();
        };
    };

    /**
     * Add a bit of pedantry to the TNT tooltip.
     * @param event
     */
    @SubscribeEvent
    public static final void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (item.equals(Items.TNT)) event.getToolTip().add(DestroyLang.translate("tooltip.tnt").style(ChatFormatting.GRAY).component());
    };

    @SubscribeEvent
    public static void onGatherTooltips(RenderTooltipEvent.GatherComponents event) {
        Minecraft mc = Minecraft.getInstance();
        ExplosiveProperties properties = null;
        if (event.getItemStack().getItem() instanceof IMixedExplosiveItem mixItem) {
            properties = mixItem.getExplosiveInventory(event.getItemStack()).getExplosiveProperties().withConditions(mixItem.getApplicableExplosionConditions());
        } else if (mc.screen instanceof MixedExplosiveScreen) {
            properties = ExplosiveProperties.ITEM_EXPLOSIVE_PROPERTIES.get(event.getItemStack().getItem());
        };
        if (properties != null) event.getTooltipElements().add(Either.right(new ExplosivePropertiesTooltip(properties)));
    };
};
