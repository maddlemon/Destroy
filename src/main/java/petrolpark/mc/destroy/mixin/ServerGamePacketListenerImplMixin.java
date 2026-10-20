package petrolpark.mc.destroy.mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;
import petrolpark.mc.destroy.core.extendedinventory.ExtendedInventory;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@MoveToPetrolparkLibrary
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerGamePacketListener {

    @Shadow
    public ServerPlayer player;

    @Shadow
    static Logger LOGGER;
    
    @Inject(
        method = "handleSetCarriedItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
            shift = Shift.AFTER
        ),
        cancellable = true
    )
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        ExtendedInventory inv = ExtendedInventory.get(player);
        if (inv.isExtendedHotbarSlot(packet.getSlot())) {
            if (player.getInventory().selected != packet.getSlot() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                player.stopUsingItem();
            };

            inv.selected = packet.getSlot();
            player.resetLastActionTime();

            ci.cancel();
        };
    };

    /**
     * Lets creative mode put Items in extended inventory slots (vanilla only accepts slots 1-45). Injects just before vanilla's
     * slot/stack validity check, like 1.20.1 did; in 1.21 that check holds the method's first {@code isEmpty()} call (it was the second).
     * The stack is read from the packet (the same object as vanilla's local, which a Block Entity's data may already have been saved to).
     */
    @Inject(
        method = "handleSetCreativeModeSlot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
            ordinal = 0
        ),
        cancellable = true
    )
    public void inHandleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        ItemStack itemstack = packet.itemStack();
        if (packet.slotNum() >= 1 && (itemstack.isEmpty() || itemstack.getDamageValue() >= 0 && !itemstack.isEmpty())) {
            player.inventoryMenu.getSlot(packet.slotNum()).setByPlayer(itemstack);
            player.inventoryMenu.broadcastChanges();
            ci.cancel();
        };
    };
};
