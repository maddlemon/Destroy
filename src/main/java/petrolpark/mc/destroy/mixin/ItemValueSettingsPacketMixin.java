package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import petrolpark.mc.destroy.core.seismology.SeismometerItem;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import petrolpark.mc.library.compat.create.core.world.item.valueSettings.ItemValueSettingsPacket;

/**
 * The library only applies this packet to {@code IValueSettingsItem}s; the Seismometer can't implement that on dedicated servers (see {@link SeismometerItem}).
 * Same validation as the library's handler.
 */
@Mixin(value = ItemValueSettingsPacket.class, remap = false)
public abstract class ItemValueSettingsPacketMixin {

    @Shadow public abstract int row();
    @Shadow public abstract int value();
    @Shadow public abstract InteractionHand hand();
    @Shadow public abstract boolean ctrlDown();

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void destroy$handleSeismometer(ServerPlayer player, CallbackInfo ci) {
        ItemStack stack = player.getItemInHand(hand());
        if (!(stack.getItem() instanceof SeismometerItem seismometer)) return;
        ci.cancel();
        ValueSettingsBoard board = seismometer.createValueSettingsBoard(player, hand(), stack);
        if (row() < 0 || row() >= board.rows().size() || value() < 0 || value() > board.maxValue()) return;
        seismometer.setValueSettings(stack, new ValueSettings(row(), value()), ctrlDown());
    };
};
