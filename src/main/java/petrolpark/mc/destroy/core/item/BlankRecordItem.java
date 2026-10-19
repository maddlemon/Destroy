package petrolpark.mc.destroy.core.item;

import java.util.List;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroySoundEvents;
import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

@MoveToPetrolparkLibrary
public class BlankRecordItem extends RecordItem {

    @SuppressWarnings("deprecation")
    public BlankRecordItem(Properties properties) {
        super(0, Destroy.datagen ? SoundEvents.STONE_STEP : DestroySoundEvents.SILENCE.getMainEvent(), properties, 5460);
    };

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        // Don't add the track title
    };
    
};
