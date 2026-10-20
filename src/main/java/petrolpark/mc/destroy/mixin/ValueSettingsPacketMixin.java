package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import petrolpark.mc.destroy.core.bettervaluesettings.BetterValueSettingsBehaviour;
import petrolpark.mc.destroy.mixin.accessor.ValueSettingsPacketAccessor;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

@Mixin(value = ValueSettingsPacket.class, remap = false)
public class ValueSettingsPacketMixin {

	/**
     * Pass Block Entities with {@link petrolpark.mc.destroy.core.bettervaluesettings.BetterValueSettingsBehaviour Smart Value Settings Behaviours} additional information.
     * Copied and minorly adjusted from the {@link com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket Create source code}.
     */
    @Overwrite(remap = false)
	protected void applySettings(ServerPlayer player, SmartBlockEntity be) {
		InteractionHand hand = ((ValueSettingsPacketAccessor)this).getInteractHand();
		Direction side = ((ValueSettingsPacketAccessor)this).getSide();
		for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;
			if (!valueSettingsBehaviour.acceptsValueSettings())
				continue;
			if (((ValueSettingsPacketAccessor)this).getBehaviourIndex() != valueSettingsBehaviour.netId()) // Create 6: several Value Settings per Block Entity
				continue;
			if (hand != null) {
				valueSettingsBehaviour.onShortInteract(player, hand, side, ((ValueSettingsPacketAccessor) this).getHitResult());
			};
            if (valueSettingsBehaviour instanceof BetterValueSettingsBehaviour smartValueSettingsBehaviour) {
                smartValueSettingsBehaviour.acceptAccessInformation(hand, side);
            };
			valueSettingsBehaviour.setValueSettings(player, new ValueSettings(((ValueSettingsPacketAccessor)this).getRow(), ((ValueSettingsPacketAccessor)this).getValue()), ((ValueSettingsPacketAccessor)this).getCtrlDown());
			return;
		}
	}

	// 1.20.1 also redirected writeSettings/readSettings to allow a null Block Hit Result; Create 6's STREAM_CODEC already does that.
};
