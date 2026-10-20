package petrolpark.mc.destroy.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import petrolpark.mc.destroy.config.DestroyClientConfigs;
import petrolpark.mc.destroy.core.extendedinventory.ExtendedInventory;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Moves the offhand slot of the hotbar out of the way of extra hotbar slots.
 * (1.20.1 also drew Chemical Poison hearts here; they are now a {@link Gui.HeartType} with sprites - see {@link petrolpark.mc.destroy.client.DestroyHeartTypes}.)
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    abstract Player getCameraPlayer();

    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");

    @Redirect(
        method = "renderItemHotbar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V"
        )
    )
    private void renderOffhandItemOffset(Gui gui, GuiGraphics pGuiGraphics, int pX, int pY, DeltaTracker deltaTracker, Player pPlayer, ItemStack pStack, int pSeed) {
        Player player = getCameraPlayer();
        if (pStack == pPlayer.getOffhandItem()) { // If we're rendering the offhand Item
            ExtendedInventory inv = ExtendedInventory.get(getCameraPlayer());
            pGuiGraphics.pose().pushPose();
            if (player.getMainArm().getOpposite() == HumanoidArm.LEFT) {
                pX -= 20 * DestroyClientConfigs.getLeftSlots(inv.getExtraHotbarSlots());
            } else {
                pX += 20 * DestroyClientConfigs.getRightSlots(inv.getExtraHotbarSlots());
            };
            pGuiGraphics.pose().popPose();
        };
        gui.renderSlot(pGuiGraphics, pX, pY, deltaTracker, pPlayer, pStack, pSeed);
    };

    @Redirect(
        method = "renderItemHotbar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
        )
    )
    private void renderOffhandBackgroundOffset(GuiGraphics pGuiGraphics, ResourceLocation sprite, int pX, int pY, int pWidth, int pHeight) {
        if (HOTBAR_OFFHAND_LEFT_SPRITE.equals(sprite) || HOTBAR_OFFHAND_RIGHT_SPRITE.equals(sprite)) { // If we're rendering the offhand background
            Player player = getCameraPlayer();
            ExtendedInventory inv = ExtendedInventory.get(getCameraPlayer());
            pGuiGraphics.pose().pushPose();
            if (player.getMainArm().getOpposite() == HumanoidArm.LEFT) {
                pX -= 20 * DestroyClientConfigs.getLeftSlots(inv.getExtraHotbarSlots());
            } else {
                pX += 20 * DestroyClientConfigs.getRightSlots(inv.getExtraHotbarSlots());
            };
            pGuiGraphics.pose().popPose();
        };
        pGuiGraphics.blitSprite(sprite, pX, pY, pWidth, pHeight);
    };
};
